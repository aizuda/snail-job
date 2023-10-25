package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.scan;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.IdempotentStrategy;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.dto.ScanTask;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.retry.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutor;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ScanTask.class, config -> {

            try {
                doScan(config);
            } catch (Exception e) {
                LogUtils.error(log, "Data scanner processing exception. [{}]", config, e);
            }

        }).build();

    }

    protected void doScan(final ScanTask scanTask) {

//        LocalDateTime lastAt = LocalDateTime.now().minusDays(systemProperties.getLastDays());
        String groupName = scanTask.getGroupName();
        Long lastId = Optional.ofNullable(getLastId(groupName)).orElse(0L);
        int retryPullPageSize = systemProperties.getRetryPullPageSize();


        AtomicInteger count = new AtomicInteger(0);
        long total = PartitionTaskUtils.process(startId -> {
                    // 没10秒触发一次扫描任务，每次扫描N次
                    int i = count.incrementAndGet();
                    // TODO 需要支持动态计算循环拉取多少次
                    if (i > 5) {
                        return Lists.newArrayList();
                    }
                    return listAvailableTasks(groupName, startId, taskActuatorScene().getTaskType().getType());
                },
                partitionTasks -> processRetryPartitionTasks(partitionTasks, scanTask), lastId);

    }

    private void processRetryPartitionTasks(List<? extends PartitionTask> partitionTasks, ScanTask scanTask) {
        if (!CollectionUtils.isEmpty(partitionTasks)) {

            // TODO 更新拉取的最大的id
            putLastId(scanTask.getGroupName(), partitionTasks.get(partitionTasks.size() - 1).getId());

            for (PartitionTask partitionTask : partitionTasks) {
                processRetryTask((RetryPartitionTask) partitionTask);

                // 已经存在时间轮里面的任务由时间轮负责调度
//                boolean existed = TimerWheelHandler.isExisted(retryTask.getGroupName(), retryTask.getUniqueId());
//                if (existed) {
//                    continue;
//                }
//
//                for (TaskExecutor taskExecutor : taskExecutors) {
//                    if (taskActuatorScene().getScene() == taskExecutor.getTaskType().getScene()) {
//                        taskExecutor.actuator(retryTask);
//                    }
//                }
            }
        } else {

//            // 数据为空则休眠5s
//            try {
//                Thread.sleep((10 / 2) * 1000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }

            putLastId(scanTask.getGroupName(), 0L);
        }
    }

    private void processRetryTask(RetryPartitionTask partitionTask) {


        // 更新触发时间, 任务进入时间轮
//        WaitStrategies.WaitStrategyEnum.getWaitStrategy(partitionTask)
//        waitStrategy.computeRetryTime(retryContext);

    }

    protected abstract TaskExecutorSceneEnum taskActuatorScene();

    protected abstract Long getLastId(String groupName);

    protected abstract void putLastId(String groupName, Long lastId);

    public List<RetryPartitionTask> listAvailableTasks(String groupName, Long lastId, Integer taskType) {
        List<RetryTask> retryTasks = accessTemplate.getRetryTaskAccess().listPage(groupName, new PageDTO<>(0, systemProperties.getRetryPullPageSize()),
                new LambdaQueryWrapper<RetryTask>()
                        .eq(RetryTask::getRetryStatus, RetryStatusEnum.RUNNING.getStatus())
                        .eq(RetryTask::getGroupName, groupName).eq(RetryTask::getTaskType, taskType)
                        // TODO 提前10秒把需要执行的任务拉取出来
                        .le(RetryTask::getNextTriggerAt, LocalDateTime.now().plusSeconds(10)).gt(RetryTask::getId, lastId)
                        // TODO 验证一下lastAt会不会改变
//                      .gt(RetryTask::getCreateDt, lastAt)
                        .orderByAsc(RetryTask::getId))
                .getRecords();

        return RetryTaskConverter.INSTANCE.toRetryPartitionTasks(retryTasks);
    }

}
