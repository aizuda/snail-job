package com.aizuda.snailjob.server.retry.task.support.generator.retry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
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
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
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
    private SystemProperties systemProperties;

    @Override
    public void taskGenerator(TaskContext taskContext) {
        SnailJobLog.LOCAL.debug("received report data. {}", JsonUtil.toJsonString(taskContext));

        RetrySceneConfig retrySceneConfig = checkAndInitScene(taskContext);

        //客户端上报任务根据幂等id去重
        List<TaskContext.TaskInfo> taskInfos = taskContext.getTaskInfos().stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(TaskContext.TaskInfo::getIdempotentId))),
                ArrayList::new));

        Set<String> idempotentIdSet = StreamUtils.toSet(taskInfos, TaskContext.TaskInfo::getIdempotentId);

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();

        // 获取相关的任务，用户幂等校验
        List<Retry> retries = retryTaskAccess.list(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getNamespaceId, taskContext.getNamespaceId())
                        .eq(Retry::getGroupName, taskContext.getGroupName())
                        .eq(Retry::getSceneName, taskContext.getSceneName())
                        .eq(Retry::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                        .eq(Retry::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
                        .in(Retry::getIdempotentId, idempotentIdSet));

        Map<String/*幂等ID*/, List<Retry>> retryTaskMap = StreamUtils.groupByKey(retries, Retry::getIdempotentId);

        List<Retry> waitInsertTasks = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (TaskContext.TaskInfo taskInfo : taskInfos) {
            Pair<List<Retry>, List<RetryTask>> pair = doConvertTask(retryTaskMap, taskContext, now, taskInfo,
                    retrySceneConfig);
            waitInsertTasks.addAll(pair.getKey());
        }

        if (CollUtil.isEmpty(waitInsertTasks)) {
            return;
        }

        Assert.isTrue(
                waitInsertTasks.size() == retryTaskAccess.insertBatch(waitInsertTasks),
                () -> new SnailJobServerException("failed to report data"));
    }

    /**
     * @param retryTaskMap
     * @param now
     * @param taskInfo
     * @param retrySceneConfig
     */
    private Pair<List<Retry>, List<RetryTask>> doConvertTask(Map<String/*幂等ID*/, List<Retry>> retryTaskMap,
                                                             TaskContext taskContext, LocalDateTime now,
                                                             TaskContext.TaskInfo taskInfo, RetrySceneConfig retrySceneConfig) {
        List<Retry> waitInsertRetryList = new ArrayList<>();
        List<RetryTask> waitInsertTaskList = new ArrayList<>();

        // 判断是否存在与幂等ID相同的任务
        List<Retry> list = retryTaskMap.getOrDefault(taskInfo.getIdempotentId(), new ArrayList<>()).stream()
                .filter(retryTask ->
                        taskContext.getGroupName().equals(retryTask.getGroupName())
                                && taskContext.getNamespaceId().equals(retryTask.getNamespaceId())
                                && taskContext.getSceneName().equals(retryTask.getSceneName())).collect(Collectors.toList());
        // 说明存在相同的任务
        if (CollUtil.isNotEmpty(list)) {
            SnailJobLog.LOCAL.warn("interrupted reporting in retrying task. [{}]", JsonUtil.toJsonString(taskInfo));
            return Pair.of(waitInsertRetryList, waitInsertTaskList);
        }

        Retry retry = RetryTaskConverter.INSTANCE.toRetryTask(taskInfo);
        retry.setNamespaceId(taskContext.getNamespaceId());
        retry.setTaskType(SyetemTaskTypeEnum.RETRY.getType());
        retry.setGroupName(taskContext.getGroupName());
        retry.setSceneName(taskContext.getSceneName());
        retry.setRetryStatus(initStatus(taskContext));
        retry.setSceneId(taskContext.getSceneId());
        retry.setGroupId(taskContext.getGroupId());
        retry.setParentId(0L);
        retry.setDeleted(0L);
        if (StrUtil.isBlank(retry.getBizNo())) {
            // 默认生成一个业务单据号方便用户查询
            retry.setBizNo(IdUtil.fastSimpleUUID());
        } else {
            retry.setBizNo(retry.getBizNo());
        }

        // 计算分桶逻辑
        retry.setBucketIndex(
                HashUtil.bkdrHash(taskContext.getGroupName() + taskContext.getSceneName() + taskInfo.getIdempotentId())
                        % systemProperties.getBucketTotal()
        );

        retry.setCreateDt(now);
        retry.setUpdateDt(now);

        if (StrUtil.isBlank(retry.getExtAttrs())) {
            retry.setExtAttrs(StrUtil.EMPTY);
        }

        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(now);
        waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
        waitStrategyContext.setDelayLevel(1);
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
        retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
        waitInsertRetryList.add(retry);

        RetryTask retryTask = RetryTaskLogConverter.INSTANCE.toRetryTask(retry);
        retryTask.setTaskType(SyetemTaskTypeEnum.RETRY.getType());
        retryTask.setCreateDt(now);
        waitInsertTaskList.add(retryTask);

        return Pair.of(waitInsertRetryList, waitInsertTaskList);
    }

    protected abstract Integer initStatus(TaskContext taskContext);

    private RetrySceneConfig checkAndInitScene(TaskContext taskContext) {
        RetrySceneConfig retrySceneConfig = accessTemplate.getSceneConfigAccess()
                .getSceneConfigByGroupNameAndSceneName(taskContext.getGroupName(), taskContext.getSceneName(),
                        taskContext.getNamespaceId());
        if (Objects.isNull(retrySceneConfig)) {
            if (taskContext.getInitScene().equals(StatusEnum.NO.getStatus())) {
                throw new SnailJobServerException(
                        "failed to report data, no scene configuration found. groupName:[{}] sceneName:[{}]",
                        taskContext.getGroupName(), taskContext.getSceneName());
            } else {
                // 若配置了默认初始化场景配置，则发现上报数据的时候未配置场景，默认生成一个场景
                retrySceneConfig = initScene(taskContext.getGroupName(), taskContext.getSceneName(), taskContext.getNamespaceId());
            }
        }

        taskContext.setSceneId(retrySceneConfig.getId());
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
        retrySceneConfig.setCbStatus(StatusEnum.NO.getStatus());
        retrySceneConfig.setCbMaxCount(DelayLevelEnum._16.getLevel());
        retrySceneConfig.setDescription("Automatically initialize scenario");
        Assert.isTrue(1 == accessTemplate.getSceneConfigAccess().insert(retrySceneConfig),
                () -> new SnailJobServerException("init scene error"));
        return retrySceneConfig;
    }

}
