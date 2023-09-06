package com.aizuda.easy.retry.server.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.enums.DelayLevelEnum;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.model.dto.RetryTaskDTO;
import com.aizuda.easy.retry.server.service.RetryService;
import com.aizuda.easy.retry.server.service.convert.RetryDeadLetterConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskConverter;
import com.aizuda.easy.retry.server.service.convert.RetryTaskLogConverter;
import com.aizuda.easy.retry.server.support.generator.IdGenerator;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.access.ConfigAccess;
import com.aizuda.easy.retry.template.datasource.access.TaskAccess;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
import com.aizuda.easy.retry.template.datasource.utils.RequestDataHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
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
    private AccessTemplate accessTemplate;
    @Autowired
    private List<IdGenerator> idGeneratorList;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Transactional
    @Override
    public Boolean reportRetry(RetryTaskDTO retryTaskDTO) {
        LogUtils.info(log, "received report data. <|>{}<|>", JsonUtil.toJsonString(retryTaskDTO));

        ConfigAccess<SceneConfig> sceneConfigAccess = accessTemplate.getSceneConfigAccess();
        SceneConfig sceneConfig = sceneConfigAccess.getSceneConfigByGroupNameAndSceneName(retryTaskDTO.getGroupName(),
            retryTaskDTO.getSceneName());
        if (Objects.isNull(sceneConfig)) {

            GroupConfig groupConfig = sceneConfigAccess.getGroupConfigByGroupName(retryTaskDTO.getGroupName());
            if (Objects.isNull(groupConfig)) {
                throw new EasyRetryServerException(
                    "failed to report data, no group configuration found. groupName:[{}]", retryTaskDTO.getGroupName());
            }

            if (groupConfig.getInitScene().equals(StatusEnum.NO.getStatus())) {
                throw new EasyRetryServerException(
                    "failed to report data, no scene configuration found. groupName:[{}] sceneName:[{}]",
                    retryTaskDTO.getGroupName(), retryTaskDTO.getSceneName());
            } else {
                // 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
                initScene(retryTaskDTO);
            }
        }

        // 此处做幂等处理，避免客户端重复多次上报
        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        long count =retryTaskAccess.count(retryTaskDTO.getGroupName(), new LambdaQueryWrapper<RetryTask>()
            .eq(RetryTask::getIdempotentId, retryTaskDTO.getIdempotentId())
            .eq(RetryTask::getGroupName, retryTaskDTO.getGroupName())
            .eq(RetryTask::getSceneName, retryTaskDTO.getSceneName())
            .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
        );
        if (0 < count) {
            LogUtils.warn(log, "interrupted reporting in retrying task. [{}]", JsonUtil.toJsonString(retryTaskDTO));
            return Boolean.TRUE;
        }

        LocalDateTime now = LocalDateTime.now();
        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(retryTaskDTO);
        retryTask.setUniqueId(getIdGenerator(retryTaskDTO.getGroupName()));
        retryTask.setTaskType(TaskTypeEnum.RETRY.getType());
        retryTask.setCreateDt(now);
        retryTask.setUpdateDt(now);

        if (StringUtils.isBlank(retryTask.getExtAttrs())) {
            retryTask.setExtAttrs(StringUtils.EMPTY);
        }

        retryTask.setNextTriggerAt(
            WaitStrategies.randomWait(1, TimeUnit.SECONDS, 60, TimeUnit.SECONDS).computeRetryTime(null));

        Assert.isTrue(1 == retryTaskAccess.insert(retryTaskDTO.getGroupName(), retryTask),
            () -> new EasyRetryServerException("failed to report data"));

        // 初始化日志
        RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(retryTask);
        retryTaskLog.setTaskType(TaskTypeEnum.RETRY.getType());
        retryTaskLog.setCreateDt(now);
        Assert.isTrue(1 == retryTaskLogMapper.insert(retryTaskLog),
            () -> new EasyRetryServerException("新增重试日志失败"));

        return Boolean.TRUE;
    }

    /**
     * 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景 backOff(退避策略): 等级策略 maxRetryCount(最大重试次数): 26 triggerInterval(间隔时间): see:
     * {@link DelayLevelEnum}
     *
     * @param retryTaskDTO 重试上报DTO
     */
    private void initScene(final RetryTaskDTO retryTaskDTO) {

        SceneConfig sceneConfig = new SceneConfig();
        sceneConfig.setGroupName(retryTaskDTO.getGroupName());
        sceneConfig.setSceneName(retryTaskDTO.getSceneName());
        sceneConfig.setSceneStatus(StatusEnum.YES.getStatus());
        sceneConfig.setBackOff(WaitStrategyEnum.DELAY_LEVEL.getBackOff());
        sceneConfig.setMaxRetryCount(DelayLevelEnum._21.getLevel());
        sceneConfig.setDescription("自动初始化场景");

        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().insert(sceneConfig),
            () -> new EasyRetryServerException("init scene error"));
    }

    @Transactional
    @Override
    public Boolean batchReportRetry(List<RetryTaskDTO> retryTaskDTOList) {
        retryTaskDTOList.forEach(this::reportRetry);
        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public Boolean moveDeadLetterAndDelFinish(String groupName) {

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        RequestDataHelper.setPartition(groupName);
        List<RetryTask> callbackRetryTasks = retryTaskAccess.listPage(groupName, new PageDTO<>(0, 100),
            new LambdaQueryWrapper<RetryTask>()
                .in(RetryTask::getRetryStatus, RetryStatusEnum.MAX_COUNT.getStatus(),
                    RetryStatusEnum.FINISH.getStatus())
                .eq(RetryTask::getTaskType, TaskTypeEnum.CALLBACK.getType())
                .eq(RetryTask::getGroupName, groupName)).getRecords();

        if (CollectionUtils.isEmpty(callbackRetryTasks)) {
            return Boolean.TRUE;
        }

        Set<String> uniqueIdSet = callbackRetryTasks.stream().map(callbackTask -> {
            String callbackTaskUniqueId = callbackTask.getUniqueId();
            return callbackTaskUniqueId.substring(callbackTaskUniqueId.lastIndexOf(StrUtil.UNDERLINE) + 1);
        }).collect(Collectors.toSet());

        List<RetryTask> retryTasks = accessTemplate.getRetryTaskAccess().list(groupName, new LambdaQueryWrapper<RetryTask>()
            .eq(RetryTask::getTaskType, TaskTypeEnum.RETRY.getType())
            .in(RetryTask::getUniqueId, uniqueIdSet)
        );

        // 迁移重试失败的数据
        List<RetryTask> waitMoveDeadLetters = new ArrayList<>();
        List<RetryTask> maxCountRetryTaskList = retryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.MAX_COUNT.getStatus())).collect(
                Collectors.toList());
        if (!CollectionUtils.isEmpty(maxCountRetryTaskList)) {
            waitMoveDeadLetters.addAll(maxCountRetryTaskList);
        }

        List<RetryTask> maxCountCallbackRetryTaskList = callbackRetryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.MAX_COUNT.getStatus())).collect(
                Collectors.toList());

        if (!CollectionUtils.isEmpty(maxCountRetryTaskList)) {
            waitMoveDeadLetters.addAll(maxCountCallbackRetryTaskList);
        }

        moveDeadLetters(groupName, waitMoveDeadLetters);

        // 删除重试完成的数据
        Set<Long> waitDelRetryFinishSet = new HashSet<>();
        Set<Long> finishRetryIdList = retryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.FINISH.getStatus()))
            .map(RetryTask::getId)
            .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(finishRetryIdList)) {
            waitDelRetryFinishSet.addAll(finishRetryIdList);
        }

        Set<Long> finishCallbackRetryIdList = callbackRetryTasks.stream()
            .filter(retryTask -> retryTask.getRetryStatus().equals(RetryStatusEnum.FINISH.getStatus()))
            .map(RetryTask::getId)
            .collect(Collectors.toSet());

        // 迁移重试失败的数据
        if (!CollectionUtils.isEmpty(finishCallbackRetryIdList)) {
            waitDelRetryFinishSet.addAll(finishCallbackRetryIdList);
        }

        if (CollectionUtils.isEmpty(waitDelRetryFinishSet)) {
            return Boolean.TRUE;
        }

        Assert.isTrue(waitDelRetryFinishSet.size() == accessTemplate.getRetryTaskAccess()
                        .delete(groupName, new LambdaQueryWrapper<RetryTask>()
                                .eq(RetryTask::getGroupName, groupName).in(RetryTask::getId, waitDelRetryFinishSet)),
            () -> new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));

        return Boolean.TRUE;
    }

    /**
     * 迁移死信队列数据
     *
     * @param groupName  组id
     * @param retryTasks 待迁移数据
     */
    private void moveDeadLetters(String groupName, List<RetryTask> retryTasks) {
        if (CollectionUtils.isEmpty(retryTasks)) {
            return;
        }

        List<RetryDeadLetter> retryDeadLetters = RetryDeadLetterConverter.INSTANCE.toRetryDeadLetter(retryTasks);
        LocalDateTime now = LocalDateTime.now();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetters) {
            retryDeadLetter.setCreateDt(now);
        }
        Assert.isTrue(retryDeadLetters.size() == accessTemplate
                        .getRetryDeadLetterAccess().batchInsert(groupName, retryDeadLetters),
            () -> new EasyRetryServerException("插入死信队列失败 [{}]", JsonUtil.toJsonString(retryDeadLetters)));

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();
        List<Long> ids = retryTasks.stream().map(RetryTask::getId).collect(Collectors.toList());
        Assert.isTrue(retryTasks.size() == retryTaskAccess.delete(groupName, new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getGroupName, groupName).in(RetryTask::getId, ids)),
            () -> new EasyRetryServerException("删除重试数据失败 [{}]", JsonUtil.toJsonString(retryTasks)));
    }

    /**
     * 获取分布式id
     *
     * @param groupName 组id
     * @return 分布式id
     */
    private String getIdGenerator(String groupName) {

        GroupConfig groupConfig = accessTemplate.getGroupConfigAccess().getGroupConfigByGroupName(groupName);
        for (final IdGenerator idGenerator : idGeneratorList) {
            if (idGenerator.supports(groupConfig.getIdGeneratorMode())) {
                return idGenerator.idGenerator(groupName);
            }
        }

        throw new EasyRetryServerException("id generator mode not configured. [{}]", groupName);
    }
}
