package com.aizuda.snailjob.server.retry.task.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.enums.DelayLevelEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.generator.id.IdGenerator;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author opensnail
 * @date 2023-07-16 11:52:39
 * @since 2.1.0
 */
@Slf4j
public abstract class AbstractGenerator implements TaskGenerator {

    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    private List<IdGenerator> idGeneratorList;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;

    @Override
    @Transactional
    public void taskGenerator(TaskContext taskContext) {
        SnailJobLog.LOCAL.debug("received report data. {}", JsonUtil.toJsonString(taskContext));

        RetrySceneConfig retrySceneConfig = checkAndInitScene(taskContext);

        //客户端上报任务根据幂等id去重
        List<TaskContext.TaskInfo> taskInfos = taskContext.getTaskInfos().stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(TaskContext.TaskInfo::getIdempotentId))),
                ArrayList::new));

        Set<String> idempotentIdSet = StreamUtils.toSet(taskInfos, TaskContext.TaskInfo::getIdempotentId);

        TaskAccess<RetryTask> retryTaskAccess = accessTemplate.getRetryTaskAccess();

        // 获取相关的任务，用户幂等校验
        List<RetryTask> retryTasks = retryTaskAccess.list(taskContext.getGroupName(), taskContext.getNamespaceId(),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getNamespaceId, taskContext.getNamespaceId())
                        .eq(RetryTask::getGroupName, taskContext.getGroupName())
                        .eq(RetryTask::getSceneName, taskContext.getSceneName())
                        .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                        .eq(RetryTask::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
                        .in(RetryTask::getIdempotentId, idempotentIdSet));

        Map<String/*幂等ID*/, List<RetryTask>> retryTaskMap = StreamUtils.groupByKey(retryTasks, RetryTask::getIdempotentId);

        List<RetryTask> waitInsertTasks = new ArrayList<>();
        List<RetryTaskLog> waitInsertTaskLogs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (TaskContext.TaskInfo taskInfo : taskInfos) {
            Pair<List<RetryTask>, List<RetryTaskLog>> pair = doConvertTask(retryTaskMap, taskContext, now, taskInfo,
                    retrySceneConfig);
            waitInsertTasks.addAll(pair.getKey());
            waitInsertTaskLogs.addAll(pair.getValue());
        }

        if (CollUtil.isEmpty(waitInsertTasks)) {
            return;
        }

        Assert.isTrue(
                waitInsertTasks.size() == retryTaskAccess.insertBatch(taskContext.getGroupName(),
                        taskContext.getNamespaceId(), waitInsertTasks),
                () -> new SnailJobServerException("failed to report data"));
        Assert.isTrue(waitInsertTaskLogs.size() == retryTaskLogMapper.insertBatch(waitInsertTaskLogs),
                () -> new SnailJobServerException("新增重试日志失败"));
    }

    /**
     * @param retryTaskMap
     * @param now
     * @param taskInfo
     * @param retrySceneConfig
     */
    private Pair<List<RetryTask>, List<RetryTaskLog>> doConvertTask(Map<String/*幂等ID*/, List<RetryTask>> retryTaskMap,
                                                                    TaskContext taskContext, LocalDateTime now,
                                                                    TaskContext.TaskInfo taskInfo, RetrySceneConfig retrySceneConfig) {
        List<RetryTask> waitInsertTasks = new ArrayList<>();
        List<RetryTaskLog> waitInsertTaskLogs = new ArrayList<>();

        // 判断是否存在与幂等ID相同的任务
        List<RetryTask> list = retryTaskMap.getOrDefault(taskInfo.getIdempotentId(), new ArrayList<>()).stream()
                .filter(retryTask ->
                        taskContext.getGroupName().equals(retryTask.getGroupName())
                                && taskContext.getNamespaceId().equals(retryTask.getNamespaceId())
                                && taskContext.getSceneName().equals(retryTask.getSceneName())).collect(Collectors.toList());
        // 说明存在相同的任务
        if (CollUtil.isNotEmpty(list)) {
            SnailJobLog.LOCAL.warn("interrupted reporting in retrying task. [{}]", JsonUtil.toJsonString(taskInfo));
            return Pair.of(waitInsertTasks, waitInsertTaskLogs);
        }

        RetryTask retryTask = RetryTaskConverter.INSTANCE.toRetryTask(taskInfo);
        retryTask.setNamespaceId(taskContext.getNamespaceId());
        retryTask.setUniqueId(getIdGenerator(taskContext.getGroupName(), taskContext.getNamespaceId()));
        retryTask.setTaskType(SyetemTaskTypeEnum.RETRY.getType());
        retryTask.setGroupName(taskContext.getGroupName());
        retryTask.setSceneName(taskContext.getSceneName());
        retryTask.setRetryStatus(initStatus(taskContext));
        retryTask.setBizNo(Optional.ofNullable(retryTask.getBizNo()).orElse(StrUtil.EMPTY));
        retryTask.setCreateDt(now);
        retryTask.setUpdateDt(now);

        if (StrUtil.isBlank(retryTask.getExtAttrs())) {
            retryTask.setExtAttrs(StrUtil.EMPTY);
        }

        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(now);
        waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
        waitStrategyContext.setDelayLevel(1);
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
        retryTask.setNextTriggerAt(DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext)));
        waitInsertTasks.add(retryTask);

        // 初始化日志
        RetryTaskLog retryTaskLog = RetryTaskLogConverter.INSTANCE.toRetryTask(retryTask);
        retryTaskLog.setTaskType(SyetemTaskTypeEnum.RETRY.getType());
        retryTaskLog.setCreateDt(now);
        waitInsertTaskLogs.add(retryTaskLog);

        return Pair.of(waitInsertTasks, waitInsertTaskLogs);
    }

    protected abstract Integer initStatus(TaskContext taskContext);

    private RetrySceneConfig checkAndInitScene(TaskContext taskContext) {
        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneName(taskContext.getGroupName(), taskContext.getSceneName(),
                        taskContext.getNamespaceId());
        if (Objects.isNull(retrySceneConfig)) {

            GroupConfig groupConfig = accessTemplate.getGroupConfigAccess()
                    .getGroupConfigByGroupName(taskContext.getGroupName(), taskContext.getNamespaceId());
            if (Objects.isNull(groupConfig)) {
                throw new SnailJobServerException(
                        "failed to report data, no group configuration found. groupName:[{}]", taskContext.getGroupName());
            }

            if (groupConfig.getInitScene().equals(StatusEnum.NO.getStatus())) {
                throw new SnailJobServerException(
                        "failed to report data, no scene configuration found. groupName:[{}] sceneName:[{}]",
                        taskContext.getGroupName(), taskContext.getSceneName());
            } else {
                // 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
                retrySceneConfig = initScene(taskContext.getGroupName(), taskContext.getSceneName(), taskContext.getNamespaceId());
            }
        }

        return retrySceneConfig;

    }

    /**
     * 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景 backOff(退避策略): 等级策略 maxRetryCount(最大重试次数): 26 triggerInterval(间隔时间): see:
     * {@link DelayLevelEnum}
     *
     * @param groupName 组名称
     * @param sceneName 场景名称
     */
    private RetrySceneConfig initScene(String groupName, String sceneName, String namespaceId) {
        RetrySceneConfig retrySceneConfig = new RetrySceneConfig();
        retrySceneConfig.setNamespaceId(namespaceId);
        retrySceneConfig.setGroupName(groupName);
        retrySceneConfig.setSceneName(sceneName);
        retrySceneConfig.setSceneStatus(StatusEnum.YES.getStatus());
        retrySceneConfig.setBackOff(WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType());
        retrySceneConfig.setMaxRetryCount(DelayLevelEnum._21.getLevel());
        retrySceneConfig.setDescription("自动初始化场景");
        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().insert(retrySceneConfig),
                () -> new SnailJobServerException("init scene error"));
        return retrySceneConfig;
    }

    /**
     * 获取分布式id
     *
     * @param groupName 组id
     * @return 分布式id
     */
    private String getIdGenerator(String groupName, String namespaceId) {

        GroupConfig groupConfig = accessTemplate.getGroupConfigAccess().getGroupConfigByGroupName(groupName, namespaceId);
        for (final IdGenerator idGenerator : idGeneratorList) {
            if (idGenerator.supports(groupConfig.getIdGeneratorMode())) {
                return idGenerator.idGenerator(groupName, namespaceId);
            }
        }

        throw new SnailJobServerException("id generator mode not configured. [{}]", groupName);
    }
}
