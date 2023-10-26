package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.scan;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.retry.task.support.timer.RetryTimerWheel;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据扫描模板类
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-05 15:44
 * @since 1.5.0
 */
@Slf4j
public abstract class AbstractScanGroup extends AbstractActor {

    @Autowired
    @Qualifier("bitSetIdempotentStrategyHandler")
    protected IdempotentStrategy<String, Integer> idempotentStrategy;
    @Autowired
    protected SystemProperties systemProperties;
    @Autowired
    protected AccessTemplate accessTemplate;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    protected List<TaskExecutor> taskExecutors;

     private static long preCostTime = 0L;
    private static long loopCount = 1L;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            // 获取开始时间
            long startTime = System.nanoTime();

            try {
                doScan(config);
            } catch (Exception e) {
                LogUtils.error(log, "Data scanner processing exception. [{}]", config, e);
            }

            // 获取结束时间
            long endTime = System.nanoTime();

            preCostTime = (endTime - startTime) / 1_000_000;
            log.info("重试任务调度耗时:[{}]", preCostTime);

        }).build();

    }

    protected void doScan(final ScanTask scanTask) {

        // 计算循环拉取的次数
        if (preCostTime > 0) {
            loopCount = (10 * 1000) / preCostTime;
            loopCount = loopCount == 0 ? 1 : loopCount;
        }

        String groupName = scanTask.getGroupName();
        Long lastId = Optional.ofNullable(getLastId(groupName)).orElse(0L);

        AtomicInteger count = new AtomicInteger(0);
        PartitionTaskUtils.process(startId -> {
                int i = count.getAndIncrement();
                if (i > loopCount) {
                    // 为空则中断处理
                    return Lists.newArrayList();
                }
                return listAvailableTasks(groupName, startId, taskActuatorScene().getTaskType().getType());
            },
            partitionTasks -> processRetryPartitionTasks(partitionTasks, scanTask), lastId);

    }

    private void processRetryPartitionTasks(List<? extends PartitionTask> partitionTasks, ScanTask scanTask) {

        StopWatch watch = new StopWatch();
        watch.start();
        if (!CollectionUtils.isEmpty(partitionTasks)) {
            putLastId(scanTask.getGroupName(), partitionTasks.get(partitionTasks.size() - 1).getId());
            for (PartitionTask partitionTask : partitionTasks) {
                processRetryTask((RetryPartitionTask) partitionTask);
            }
        } else {
            putLastId(scanTask.getGroupName(), 0L);
        }

        watch.getTotalTimeMillis();

    }

    private void processRetryTask(RetryPartitionTask partitionTask) {

        RetryTask retryTask = new RetryTask();
        retryTask.setNextTriggerAt(calculateNextTriggerTime(partitionTask));
        retryTask.setId(partitionTask.getId());
        accessTemplate.getRetryTaskAccess().updateById(partitionTask.getGroupName(), retryTask);

        long delay = partitionTask.getNextTriggerAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            - System.currentTimeMillis();
        RetryTimerWheel.register(retryTask.getGroupName(), retryTask.getUniqueId(), timerTask(partitionTask), delay,
            TimeUnit.MILLISECONDS);
    }

    protected abstract TaskExecutorSceneEnum taskActuatorScene();

    protected abstract Long getLastId(String groupName);

    protected abstract void putLastId(String groupName, Long lastId);

    protected abstract LocalDateTime calculateNextTriggerTime(RetryPartitionTask partitionTask);

    protected abstract TimerTask timerTask(RetryPartitionTask partitionTask);

    public List<RetryPartitionTask> listAvailableTasks(String groupName, Long lastId, Integer taskType) {
        List<RetryTask> retryTasks = accessTemplate.getRetryTaskAccess()
            .listPage(groupName, new PageDTO<>(0, systemProperties.getRetryPullPageSize()),
                new LambdaQueryWrapper<RetryTask>()
                    .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                    .eq(RetryTask::getGroupName, groupName).eq(RetryTask::getTaskType, taskType)
                    .le(RetryTask::getNextTriggerAt, LocalDateTime.now().plusSeconds(10)).gt(RetryTask::getId, lastId)
                    .orderByAsc(RetryTask::getId))
            .getRecords();

        return RetryTaskConverter.INSTANCE.toRetryPartitionTasks(retryTasks);
    }

}
