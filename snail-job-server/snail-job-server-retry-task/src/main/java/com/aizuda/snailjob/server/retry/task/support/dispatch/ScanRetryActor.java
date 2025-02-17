package com.aizuda.snailjob.server.retry.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Component(ActorGenerator.SCAN_RETRY_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class ScanRetryActor extends AbstractActor {
    private final SystemProperties systemProperties;
    private final AccessTemplate accessTemplate;
    private final RetryMapper retryMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    private void doScan(final ScanTask scanTask) {
        PartitionTaskUtils.process(startId ->
                        listAvailableTasks(startId, scanTask.getBuckets()),
                partitionTasks -> processRetryPartitionTasks(partitionTasks, scanTask),
                0);

    }

    private void processRetryPartitionTasks(List<? extends PartitionTask> partitionTasks, final ScanTask scanTask) {
        if (CollUtil.isEmpty(partitionTasks)) {
            return;
        }

        // 批次查询场景
        Map<String, RetrySceneConfig> sceneConfigMap = getSceneConfigMap(partitionTasks);

        List<Retry> waitUpdateRetries = new ArrayList<>();
        List<RetryTaskPrepareDTO> waitExecRetries = new ArrayList<>();
        for (PartitionTask task : partitionTasks) {
            RetryPartitionTask retryPartitionTask = (RetryPartitionTask) task;
            RetrySceneConfig retrySceneConfig = sceneConfigMap.get(retryPartitionTask.getSceneName());
            if (Objects.isNull(retrySceneConfig)) {
                continue;
            }

            processRetry(retryPartitionTask, retrySceneConfig, waitExecRetries, waitUpdateRetries);

        }

        if (CollUtil.isEmpty(waitUpdateRetries)) {
            return;
        }

        // 批量更新
        retryMapper.updateBatchNextTriggerAtById(waitUpdateRetries);

        for (RetryTaskPrepareDTO retryTaskPrepareDTO: waitExecRetries) {
            // 准备阶段执行
            ActorRef actorRef = ActorGenerator.retryTaskPrepareActor();
            actorRef.tell(retryTaskPrepareDTO, actorRef);
        }

    }

    /**
     * 查询场景配置或者退避策略
     *
     * @param partitionTasks 待处理任务列表
     * @return <SceneName, RetrySceneConfig>
     */
    private Map<String, RetrySceneConfig> getSceneConfigMap(final List<? extends PartitionTask> partitionTasks) {
        Set<String> sceneNameSet = StreamUtils.toSet(partitionTasks,
                partitionTask -> ((RetryPartitionTask) partitionTask).getSceneName());
        List<RetrySceneConfig> retrySceneConfigs = accessTemplate.getSceneConfigAccess()
                .list(new LambdaQueryWrapper<RetrySceneConfig>()
                        .select(RetrySceneConfig::getBackOff, RetrySceneConfig::getTriggerInterval, RetrySceneConfig::getSceneName)
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));
        return StreamUtils.toIdentityMap(retrySceneConfigs, RetrySceneConfig::getSceneName);
    }

    private void processRetry(RetryPartitionTask partitionTask, RetrySceneConfig retrySceneConfig, List<RetryTaskPrepareDTO> waitExecRetries, List<Retry> waitUpdateRetries) {
        Retry retry = new Retry();
        retry.setNextTriggerAt(calculateNextTriggerTime(partitionTask, retrySceneConfig));
        retry.setId(partitionTask.getId());
        waitUpdateRetries.add(retry);

        RetryTaskPrepareDTO retryTaskPrepareDTO = RetryTaskConverter.INSTANCE.toRetryTaskPrepareDTO(partitionTask);
        retryTaskPrepareDTO.setBlockStrategy(retrySceneConfig.getBackOff());
        waitExecRetries.add(retryTaskPrepareDTO);
    }

    protected Long calculateNextTriggerTime(RetryPartitionTask partitionTask, RetrySceneConfig retrySceneConfig) {
        // 更新下次触发时间

        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();

        long now = DateUtils.toNowMilli();
        long nextTriggerAt = partitionTask.getNextTriggerAt();
        if ((nextTriggerAt + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD)) < now) {
            nextTriggerAt = now;
            partitionTask.setNextTriggerAt(nextTriggerAt);
        }

        waitStrategyContext.setNextTriggerAt(DateUtils.toEpochMilli(nextTriggerAt));
        waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
        waitStrategyContext.setDelayLevel(partitionTask.getRetryCount() + 1);
        // 更新触发时间, 任务进入时间轮
        WaitStrategy waitStrategy;
        if (SyetemTaskTypeEnum.CALLBACK.getType().equals(partitionTask.getTaskType())) {
            waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getCbTriggerType());
        } else {
            waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
        }

        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    public List<RetryPartitionTask> listAvailableTasks(Long startId, Set<Integer> buckets) {
        List<Retry> retries = accessTemplate.getRetryAccess()
                .listPage(new PageDTO<>(0, systemProperties.getRetryPullPageSize()),
                        new LambdaQueryWrapper<Retry>()
                                .select(Retry::getId, Retry::getNextTriggerAt, Retry::getGroupName, Retry::getRetryCount,
                                        Retry::getSceneName, Retry::getNamespaceId, Retry::getTaskType)
                                .eq(Retry::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                                .in(Retry::getBucketIndex, buckets)
                                .le(Retry::getNextTriggerAt, DateUtils.toNowMilli() + DateUtils.toEpochMilli(SystemConstants.SCHEDULE_PERIOD))
                                .gt(Retry::getId, startId)
                                .orderByAsc(Retry::getId))
                .getRecords();

        return RetryTaskConverter.INSTANCE.toRetryPartitionTasks(retries);
    }
}
