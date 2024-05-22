package com.aizuda.snailjob.server.retry.task.support.dispatch.actor.scan;

import akka.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.dto.ScanTask;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerWheel;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据扫描模板类
 *
 * @author: opensnail
 * @date : 2023-06-05 15:44
 * @since 1.5.0
 */
@Slf4j
public abstract class AbstractScanGroup extends AbstractActor {

    @Autowired
    protected SystemProperties systemProperties;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    protected List<TaskExecutor> taskExecutors;
    @Autowired
    protected RetryTaskMapper retryTaskMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            // 获取开始时间
            long startTime = System.nanoTime();

            try {
                doScan(config);
            } catch (Exception e) {
                SnailJobLog.LOCAL.error("Data scanner processing exception. [{}]", config, e);
            }

            // 获取结束时间
            long endTime = System.nanoTime();

            preCostTime().set((endTime - startTime) / 1_000_000);

        }).build();

    }

    protected void doScan(final ScanTask scanTask) {

        // 计算循环拉取的次数
        if (preCostTime().get() > 0) {
            long loopCount = Math.max((SystemConstants.SCHEDULE_PERIOD * 1000) / preCostTime().get(), 1);
            loopCount = Math.min(loopCount, systemProperties.getRetryMaxPullCount());
            prePullCount().set(loopCount);
        }

        String groupName = scanTask.getGroupName();
        String namespaceId = scanTask.getNamespaceId();
        Long lastId = Optional.ofNullable(getLastId(groupName)).orElse(0L);

        AtomicInteger count = new AtomicInteger(0);
        long total = PartitionTaskUtils.process(
                startId -> listAvailableTasks(groupName, namespaceId, startId, taskActuatorScene().getTaskType().getType()),
                partitionTasks1 -> processRetryPartitionTasks(partitionTasks1, scanTask), partitionTasks -> {
                    if (CollUtil.isEmpty(partitionTasks)) {
                        putLastId(scanTask.getGroupName(), 0L);
                        return Boolean.TRUE;
                    }

                    // 超过最大的拉取次数则中断
                    if (count.incrementAndGet() >= prePullCount().get()) {
                        putLastId(scanTask.getGroupName(), partitionTasks.get(partitionTasks.size() - 1).getId());
                        return Boolean.TRUE;
                    }

                    return false;
                }, lastId);

//        log.warn(this.getClass().getName() + " retry scan end. groupName:[{}] startId:[{}] preCostTime:[{}] total:[{}] realPullCount:[{}]",
//                groupName, lastId, preCostTime().get(), total, count.get());

    }

    private void processRetryPartitionTasks(List<? extends PartitionTask> partitionTasks, final ScanTask scanTask) {

        // 批次查询场景
        Map<String, RetrySceneConfig> sceneConfigMap = getSceneConfigMap(partitionTasks, scanTask);

        List<RetryTask> waitUpdateRetryTasks = new ArrayList<>();
        for (PartitionTask task : partitionTasks) {
            RetryPartitionTask retryPartitionTask = (RetryPartitionTask) task;
            RetrySceneConfig retrySceneConfig = sceneConfigMap.get(retryPartitionTask.getSceneName());
            if (Objects.isNull(retrySceneConfig)) {
                continue;
            }

            RetryTask retryTask = processRetryTask(retryPartitionTask, retrySceneConfig);
            waitUpdateRetryTasks.add(retryTask);
        }

        // 批量更新
        retryTaskMapper.updateBatchNextTriggerAtById(scanTask.getGroupPartition(), waitUpdateRetryTasks);

        long nowMilli = DateUtils.toNowMilli();
        for (PartitionTask partitionTask : partitionTasks) {
            RetryPartitionTask retryPartitionTask = (RetryPartitionTask) partitionTask;
            long delay = DateUtils.toEpochMilli(retryPartitionTask.getNextTriggerAt()) - nowMilli - nowMilli % 100;
            RetryTimerWheel.register(
                    Pair.of(retryPartitionTask.getGroupName(), retryPartitionTask.getNamespaceId()),
                    retryPartitionTask.getUniqueId(),
                    timerTask(retryPartitionTask),
                    delay,
                    TimeUnit.MILLISECONDS);
        }

    }

    private Map<String, RetrySceneConfig> getSceneConfigMap(final List<? extends PartitionTask> partitionTasks, ScanTask scanTask) {
        Set<String> sceneNameSet = StreamUtils.toSet(partitionTasks,
                partitionTask -> ((RetryPartitionTask) partitionTask).getSceneName());
        List<RetrySceneConfig> retrySceneConfigs = accessTemplate.getSceneConfigAccess()
                .list(new LambdaQueryWrapper<RetrySceneConfig>()
                        .select(RetrySceneConfig::getBackOff, RetrySceneConfig::getTriggerInterval, RetrySceneConfig::getSceneName)
                        .eq(RetrySceneConfig::getNamespaceId, scanTask.getNamespaceId())
                        .eq(RetrySceneConfig::getGroupName, scanTask.getGroupName())
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));
        return StreamUtils.toIdentityMap(retrySceneConfigs, RetrySceneConfig::getSceneName);
    }

    private RetryTask processRetryTask(RetryPartitionTask partitionTask, RetrySceneConfig retrySceneConfig) {
        RetryTask retryTask = new RetryTask();
        retryTask.setNextTriggerAt(calculateNextTriggerTime(partitionTask, retrySceneConfig));
        retryTask.setId(partitionTask.getId());
        return retryTask;
    }

    protected abstract TaskExecutorSceneEnum taskActuatorScene();

    protected abstract Long getLastId(String groupName);

    protected abstract void putLastId(String groupName, Long lastId);

    protected abstract LocalDateTime calculateNextTriggerTime(RetryPartitionTask partitionTask,
                                                              final RetrySceneConfig retrySceneConfig);

    protected abstract TimerTask timerTask(RetryPartitionTask partitionTask);

    protected abstract AtomicLong preCostTime();

    protected abstract AtomicLong prePullCount();

    public List<RetryPartitionTask> listAvailableTasks(String groupName, String namespaceId, Long lastId, Integer taskType) {
        List<RetryTask> retryTasks = accessTemplate.getRetryTaskAccess()
                .listPage(groupName, namespaceId, new PageDTO<>(0, systemProperties.getRetryPullPageSize()),
                        new LambdaQueryWrapper<RetryTask>()
                                .select(RetryTask::getId, RetryTask::getNextTriggerAt, RetryTask::getUniqueId,
                                        RetryTask::getGroupName, RetryTask::getRetryCount, RetryTask::getSceneName,
                                        RetryTask::getNamespaceId)
                                .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                                .eq(RetryTask::getGroupName, groupName)
                                .eq(RetryTask::getNamespaceId, namespaceId)
                                .eq(RetryTask::getTaskType, taskType)
                                .le(RetryTask::getNextTriggerAt, LocalDateTime.now().plusSeconds(SystemConstants.SCHEDULE_PERIOD))
                                .gt(RetryTask::getId, lastId)
                                .orderByAsc(RetryTask::getId))
                .getRecords();

        return RetryTaskConverter.INSTANCE.toRetryPartitionTasks(retryTasks);
    }

}
