package com.aizuda.snail.job.server.retry.task.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snail.job.common.core.enums.RetryStatusEnum;
import com.aizuda.snail.job.common.core.enums.StatusEnum;
import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.server.common.enums.DelayLevelEnum;
import com.aizuda.snail.job.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import com.aizuda.snail.job.server.common.generator.id.IdGenerator;
import com.aizuda.snail.job.server.common.util.DateUtils;
import com.aizuda.snail.job.server.retry.task.generator.task.TaskContext.TaskInfo;
import com.aizuda.snail.job.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snail.job.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.snail.job.server.common.WaitStrategy;
import com.aizuda.snail.job.server.common.strategy.WaitStrategies;
import com.aizuda.snail.job.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snail.job.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snail.job.template.datasource.access.AccessTemplate;
import com.aizuda.snail.job.template.datasource.access.TaskAccess;
import com.aizuda.snail.job.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snail.job.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTask;
import com.aizuda.snail.job.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.snail.job.template.datasource.persistence.po.SceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
       EasyRetryLog.LOCAL.debug("received report data. {}", JsonUtil.toJsonString(taskContext));

        SceneConfig sceneConfig = checkAndInitScene(taskContext);

        //客户端上报任务根据幂等id去重
        List<TaskContext.TaskInfo> taskInfos = taskContext.getTaskInfos().stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(TaskContext.TaskInfo::getIdempotentId))),
                ArrayList::new));

        Set<String> idempotentIdSet = taskInfos.stream().map(TaskContext.TaskInfo::getIdempotentId)
                .collect(Collectors.toSet());

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

        Map<String/*幂等ID*/, List<RetryTask>> retryTaskMap = retryTasks.stream()
                .collect(Collectors.groupingBy(RetryTask::getIdempotentId));

        List<RetryTask> waitInsertTasks = new ArrayList<>();
        List<RetryTaskLog> waitInsertTaskLogs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (TaskContext.TaskInfo taskInfo : taskInfos) {
            Pair<List<RetryTask>, List<RetryTaskLog>> pair = doConvertTask(retryTaskMap, taskContext, now, taskInfo,
                    sceneConfig);
            waitInsertTasks.addAll(pair.getKey());
            waitInsertTaskLogs.addAll(pair.getValue());
        }

        if (CollectionUtils.isEmpty(waitInsertTasks)) {
            return;
        }

        Assert.isTrue(
                waitInsertTasks.size() == retryTaskAccess.batchInsert(taskContext.getGroupName(),
                        taskContext.getNamespaceId(), waitInsertTasks),
                () -> new EasyRetryServerException("failed to report data"));
        Assert.isTrue(waitInsertTaskLogs.size() == retryTaskLogMapper.batchInsert(waitInsertTaskLogs),
                () -> new EasyRetryServerException("新增重试日志失败"));
    }

    /**
     * @param retryTaskMap
     * @param now
     * @param taskInfo
     * @param sceneConfig
     */
    private Pair<List<RetryTask>, List<RetryTaskLog>> doConvertTask(Map<String/*幂等ID*/, List<RetryTask>> retryTaskMap,
                                                                    TaskContext taskContext, LocalDateTime now,
                                                                    TaskInfo taskInfo, SceneConfig sceneConfig) {
        List<RetryTask> waitInsertTasks = new ArrayList<>();
        List<RetryTaskLog> waitInsertTaskLogs = new ArrayList<>();

        // 判断是否存在与幂等ID相同的任务
        List<RetryTask> list = retryTaskMap.getOrDefault(taskInfo.getIdempotentId(), new ArrayList<>()).stream()
                .filter(retryTask ->
                        taskContext.getGroupName().equals(retryTask.getGroupName())
                                && taskContext.getNamespaceId().equals(retryTask.getNamespaceId())
                                && taskContext.getSceneName().equals(retryTask.getSceneName())).collect(Collectors.toList());
        // 说明存在相同的任务
        if (!CollectionUtils.isEmpty(list)) {
           EasyRetryLog.LOCAL.warn("interrupted reporting in retrying task. [{}]", JsonUtil.toJsonString(taskInfo));
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
        waitStrategyContext.setTriggerInterval(sceneConfig.getTriggerInterval());
        waitStrategyContext.setDelayLevel(1);
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(sceneConfig.getBackOff());
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

    private SceneConfig checkAndInitScene(TaskContext taskContext) {
        SceneConfig sceneConfig = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneName(taskContext.getGroupName(), taskContext.getSceneName(),
                        taskContext.getNamespaceId());
        if (Objects.isNull(sceneConfig)) {

            GroupConfig groupConfig = accessTemplate.getGroupConfigAccess()
                    .getGroupConfigByGroupName(taskContext.getGroupName(), taskContext.getNamespaceId());
            if (Objects.isNull(groupConfig)) {
                throw new EasyRetryServerException(
                        "failed to report data, no group configuration found. groupName:[{}]", taskContext.getGroupName());
            }

            if (groupConfig.getInitScene().equals(StatusEnum.NO.getStatus())) {
                throw new EasyRetryServerException(
                        "failed to report data, no scene configuration found. groupName:[{}] sceneName:[{}]",
                        taskContext.getGroupName(), taskContext.getSceneName());
            } else {
                // 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
                sceneConfig = initScene(taskContext.getGroupName(), taskContext.getSceneName(), taskContext.getNamespaceId());
            }
        }

        return sceneConfig;

    }

    /**
     * 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景 backOff(退避策略): 等级策略 maxRetryCount(最大重试次数): 26 triggerInterval(间隔时间): see:
     * {@link DelayLevelEnum}
     *
     * @param groupName 组名称
     * @param sceneName 场景名称
     */
    private SceneConfig initScene(String groupName, String sceneName, String namespaceId) {
        SceneConfig sceneConfig = new SceneConfig();
        sceneConfig.setNamespaceId(namespaceId);
        sceneConfig.setGroupName(groupName);
        sceneConfig.setSceneName(sceneName);
        sceneConfig.setSceneStatus(StatusEnum.YES.getStatus());
        sceneConfig.setBackOff(WaitStrategies.WaitStrategyEnum.DELAY_LEVEL.getType());
        sceneConfig.setMaxRetryCount(DelayLevelEnum._21.getLevel());
        sceneConfig.setDescription("自动初始化场景");
        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().insert(sceneConfig),
                () -> new EasyRetryServerException("init scene error"));
        return sceneConfig;
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

        throw new EasyRetryServerException("id generator mode not configured. [{}]", groupName);
    }
}
