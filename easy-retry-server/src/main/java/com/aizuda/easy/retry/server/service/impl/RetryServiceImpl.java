package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryDeadLetterMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.aizuda.easy.retry.server.persistence.mybatis.po.GroupConfig;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.SceneConfig;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.server.persistence.support.RetryTaskAccess;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.config.RequestDataHelper;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.service.RetryService;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 重试服务层实现
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 15:19
 */
@Service
@Slf4j
public class RetryServiceImpl implements RetryService {

    @Autowired
    @Qualifier("retryTaskAccessProcessor")
    private RetryTaskAccess<RetryTask> retryTaskAccess;

    @Autowired
    @Qualifier("configAccessProcessor")
    private ConfigAccess configAccess;

    @Autowired
    private RetryTaskMapper retryTaskMapper;
    @Autowired
    private RetryDeadLetterMapper retryDeadLetterMapper;

    @Transactional
    @Override
    public Boolean reportRetry(RetryTaskDTO retryTaskDTO) {
        LogUtils.warn(log, "接收上报数据 [{}]", JsonUtil.toJsonString(retryTaskDTO));

        SceneConfig sceneConfig = configAccess.getSceneConfigByGroupNameAndSceneName(retryTaskDTO.getGroupName(), retryTaskDTO.getSceneName());
        if (Objects.isNull(sceneConfig)) {
            throw new EasyRetryServerException("上报数据失败, 未查到场景配置 [{}]", retryTaskDTO);
        }

        RequestDataHelper.setPartition(retryTaskDTO.getGroupName());
        // 此处做幂等处理，避免客户端重复多次上报
        long count = retryTaskMapper.selectCount(new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getBizId, retryTaskDTO.getBizId())
                .eq(RetryTask::getGroupName, retryTaskDTO.getGroupName())
                .eq(RetryTask::getSceneName, retryTaskDTO.getSceneName())
                .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                );
        if (0 < count) {
            LogUtils.warn(log, "存在重试中的任务中断上报 [{}]", JsonUtil.toJsonString(retryTaskDTO));
            return Boolean.TRUE;
        }

        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTaskDTO);
        retryTask.setCreateDt(LocalDateTime.now());
        retryTask.setUpdateDt(LocalDateTime.now());

        if (StringUtils.isBlank(retryTask.getExtAttrs())) {
            retryTask.setExtAttrs(StringUtils.EMPTY);
        }

        retryTask.setNextTriggerAt(WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));

        Assert.isTrue(1 ==  retryTaskAccess.saveRetryTask(retryTask), () ->  new EasyRetryServerException("上报数据失败"));
        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public Boolean batchReportRetry(List<RetryTaskDTO> retryTaskDTOList) {
        retryTaskDTOList.forEach(this::reportRetry);
        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public Boolean moveDeadLetterAndDelFinish(String groupId) {

        // 清除重试完成的数据
        clearFinishRetryData(groupId);

        List<RetryTask> retryTasks = retryTaskAccess.listRetryTaskByRetryCount(groupId, RetryStatusEnum.MAX_RETRY_COUNT.getStatus());
        if (CollectionUtils.isEmpty(retryTasks)) {
            return Boolean.TRUE;
        }

        // 迁移重试失败的数据
        moveDeadLetters(groupId, retryTasks);

        return Boolean.TRUE;
    }

    /**
     * 迁移死信队列数据
     *
     * @param groupName 组id
     * @param retryTasks 待迁移数据
     */
    private void moveDeadLetters(String groupName, List<RetryTask> retryTasks) {

        List<RetryDeadLetter> retryDeadLetters = new ArrayList<>();

        for (RetryTask retryTask : retryTasks) {
            RetryDeadLetter retryDeadLetter = new RetryDeadLetter();
            BeanUtils.copyProperties(retryTask, retryDeadLetter);
            retryDeadLetter.setId(null);
            retryDeadLetter.setCreateDt(LocalDateTime.now());
            retryDeadLetters.add(retryDeadLetter);
        }

        GroupConfig groupConfig = configAccess.getGroupConfigByGroupName(groupName);
        Assert.isTrue(retryDeadLetters.size() == retryDeadLetterMapper.insertBatch(retryDeadLetters, groupConfig.getGroupPartition()),
            () -> new EasyRetryServerException("插入死信队列失败 [{}]" , JsonUtil.toJsonString(retryDeadLetters)));

        List<Long> ids = retryTasks.stream().map(RetryTask::getId).collect(Collectors.toList());
        Assert.isTrue(retryTasks.size() ==  retryTaskMapper.deleteBatch(ids, groupConfig.getGroupPartition()),
            () ->  new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));
    }

    /**
     * 请求已完成的重试数据
     *
     * @param groupId 组id
     */
    private void clearFinishRetryData(String groupId) {
        // 将已经重试完成的数据删除
        retryTaskAccess.deleteByDelayLevel(groupId, RetryStatusEnum.FINISH.getStatus());
    }
}
