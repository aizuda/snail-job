package com.x.retry.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.x.retry.common.core.enums.RetryStatusEnum;
import com.x.retry.server.config.RequestDataHelper;
import com.x.retry.server.model.dto.RetryTaskDTO;
import com.x.retry.common.core.util.Assert;
import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.server.exception.XRetryServerException;
import com.x.retry.server.persistence.mybatis.mapper.RetryDeadLetterMapper;
import com.x.retry.server.persistence.mybatis.mapper.RetryTaskMapper;
import com.x.retry.server.persistence.mybatis.po.GroupConfig;
import com.x.retry.server.persistence.mybatis.po.RetryDeadLetter;
import com.x.retry.server.persistence.mybatis.po.RetryTask;
import com.x.retry.server.persistence.mybatis.po.SceneConfig;
import com.x.retry.server.persistence.support.ConfigAccess;
import com.x.retry.server.persistence.support.RetryTaskAccess;
import com.x.retry.server.service.RetryService;
import com.x.retry.server.service.convert.RetryTaskConverter;
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
import java.util.stream.Collectors;

/**
 * 重试服务层实现
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-26 15:19
 */
@Service
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

        SceneConfig sceneConfig = configAccess.getSceneConfigByGroupNameAndSceneName(retryTaskDTO.getGroupName(), retryTaskDTO.getSceneName());
        if (Objects.isNull(sceneConfig)) {
            throw new XRetryServerException("上报数据失败, 未查到场景配置 [{}]", retryTaskDTO);
        }

        RequestDataHelper.setPartition(retryTaskDTO.getGroupName());
        // 此处做幂等处理，避免客户端重复多次上报
        long count = retryTaskMapper.selectCount(new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getBizId, retryTaskDTO.getBizId())
                .eq(RetryTask::getGroupName, retryTaskDTO.getGroupName())
                .eq(RetryTask::getSceneName, retryTaskDTO.getSceneName())
                );
        if (0 < count) {
            return Boolean.TRUE;
        }

        RetryTaskConverter converter = new RetryTaskConverter();
        RetryTask retryTask = converter.convert(retryTaskDTO);

        Assert.isTrue(1 ==  retryTaskAccess.saveRetryTask(retryTask), new XRetryServerException("上报数据失败"));
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

        List<RetryTask> retryTasks = retryTaskAccess.listRetryTaskByRetryCount(groupId, RetryStatusEnum.MAX_RETRY_COUNT.getLevel());
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
                new XRetryServerException("插入死信队列失败 [{}]" , JsonUtil.toJsonString(retryDeadLetters)));

        List<Long> ids = retryTasks.stream().map(RetryTask::getId).collect(Collectors.toList());
        Assert.isTrue(retryTasks.size() ==  retryTaskMapper.deleteBatch(ids, groupConfig.getGroupPartition()),
                new XRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));
    }

    /**
     * 请求已完成的重试数据
     *
     * @param groupId 组id
     */
    private void clearFinishRetryData(String groupId) {
        // 将已经重试完成的数据删除
        retryTaskAccess.deleteByDelayLevel(groupId, RetryStatusEnum.FINISH.getLevel());
    }
}
