package com.aizuda.snailjob.server.retry.task.support.dispatch;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.enums.RetryTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.handler.RateLimiterHandler;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.GroupConfigMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private final GroupConfigMapper groupConfigMapper;
    private final RateLimiterHandler rateLimiterHandler;

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
        PartitionTaskUtils.process(startId -> listAvailableTasks(startId, scanTask.getBuckets()),
                this::processRetryPartitionTasks, this::stopCondition, 0);
    }

    /**
     * 拉取任务停止判断
     *
     * @param partitionTasks RetryPartitionTask
     * @return true-停止拉取 false-继续拉取
     */
    private boolean stopCondition(List<? extends PartitionTask> partitionTasks) {
        if (CollectionUtils.isEmpty(partitionTasks)) {
            return true;
        }

        boolean b = rateLimiterHandler.tryAcquire(partitionTasks.size());
        log.warn("获取令牌, b:[{}] size:[{}]", b, partitionTasks.size());
        if (!b) {
            log.warn("当前节点触发限流");
            return true;
        }

        return false;
    }

    private void processRetryPartitionTasks(List<? extends PartitionTask> partitionTasks) {
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
                        .select(RetrySceneConfig::getBackOff, RetrySceneConfig::getTriggerInterval,
                                RetrySceneConfig::getSceneName, RetrySceneConfig::getCbTriggerType,
                                RetrySceneConfig::getCbTriggerInterval, RetrySceneConfig::getExecutorTimeout)
                        .eq(RetrySceneConfig::getSceneStatus, StatusEnum.YES.getStatus())
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));
        return StreamUtils.toIdentityMap(retrySceneConfigs, RetrySceneConfig::getSceneName);
    }

    private void processRetry(RetryPartitionTask partitionTask, RetrySceneConfig retrySceneConfig, List<RetryTaskPrepareDTO> waitExecRetries, List<Retry> waitUpdateRetries) {
        Retry retry = new Retry();
        retry.setNextTriggerAt(calculateNextTriggerTime(partitionTask, retrySceneConfig));
        retry.setId(partitionTask.getId());
        waitUpdateRetries.add(retry);

        RetryTaskPrepareDTO retryTaskPrepareDTO = RetryTaskConverter.INSTANCE.toRetryTaskPrepareDTO(partitionTask);
        retryTaskPrepareDTO.setBlockStrategy(retrySceneConfig.getBlockStrategy());
        retryTaskPrepareDTO.setExecutorTimeout(retrySceneConfig.getExecutorTimeout());
        retryTaskPrepareDTO.setRetryTaskExecutorScene(RetryTaskExecutorSceneEnum.AUTO_RETRY.getScene());
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

        waitStrategyContext.setNextTriggerAt(nextTriggerAt);
        waitStrategyContext.setDelayLevel(partitionTask.getRetryCount() + 1);

        // 更新触发时间, 任务进入时间轮
        WaitStrategy waitStrategy;
        if (SyetemTaskTypeEnum.CALLBACK.getType().equals(partitionTask.getTaskType())) {
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getCbTriggerInterval());
            waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getCbTriggerType());
        } else {
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
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

        // 过滤已关闭的组
        if (CollUtil.isNotEmpty(retries)) {
            List<String> groupConfigs = StreamUtils.toList(groupConfigMapper.selectList(new LambdaQueryWrapper<GroupConfig>()
                            .select(GroupConfig::getGroupName)
                            .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
                            .in(GroupConfig::getGroupName, StreamUtils.toSet(retries, Retry::getGroupName))),
                    GroupConfig::getGroupName);
            retries = retries.stream().filter(retry -> groupConfigs.contains(retry.getGroupName())).collect(Collectors.toList());
        }

        return RetryTaskConverter.INSTANCE.toRetryPartitionTasks(retries);
    }
}
