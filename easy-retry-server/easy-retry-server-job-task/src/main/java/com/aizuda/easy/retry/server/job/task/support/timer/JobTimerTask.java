package com.aizuda.easy.retry.server.job.task.support.timer;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.easy.retry.server.job.task.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:28
 * @since 2.4.0
 */
@AllArgsConstructor
@Slf4j
public class JobTimerTask implements TimerTask {

    private JobTimerTaskDTO jobTimerTaskDTO;

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        log.info("开始执行任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), jobTimerTaskDTO.getTaskBatchId());

        executor.execute(() -> {
            Job job = null;
            try {
                // 清除时间轮的缓存
                JobTimerWheel.clearCache(jobTimerTaskDTO.getTaskBatchId());

                JobMapper jobMapper = SpringContext.getBeanByType(JobMapper.class);
                job = jobMapper.selectOne(new LambdaQueryWrapper<Job>()
                        .eq(Job::getJobStatus, StatusEnum.YES.getStatus())
                        .eq(Job::getId, jobTimerTaskDTO.getJobId())
                );

                int taskStatus = JobTaskBatchStatusEnum.RUNNING.getStatus();
                int operationReason = JobOperationReasonEnum.NONE.getReason();
                if (Objects.isNull(job)) {
                    log.warn("任务已经关闭不允许执行. jobId:[{}]", jobTimerTaskDTO.getJobId());
                    taskStatus = JobTaskBatchStatusEnum.CANCEL.getStatus();
                    operationReason = JobOperationReasonEnum.JOB_CLOSED.getReason();
                }

                JobTaskBatchMapper jobTaskBatchMapper = SpringContext.getBeanByType(JobTaskBatchMapper.class);
                JobTaskBatch jobTaskBatch = new JobTaskBatch();
                jobTaskBatch.setId(jobTimerTaskDTO.getTaskBatchId());
                jobTaskBatch.setExecutionAt(LocalDateTime.now());
                jobTaskBatch.setTaskBatchStatus(taskStatus);
                jobTaskBatch.setOperationReason(operationReason);
                Assert.isTrue(1 == jobTaskBatchMapper.updateById(jobTaskBatch),
                        () -> new EasyRetryServerException("更新任务失败"));

                // 如果任务已经关闭则不需要执行
                if (Objects.isNull(job)) {
                    return;
                }

                TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
                taskExecuteDTO.setTaskBatchId(jobTimerTaskDTO.getTaskBatchId());
                taskExecuteDTO.setGroupName(jobTimerTaskDTO.getGroupName());
                taskExecuteDTO.setJobId(jobTimerTaskDTO.getJobId());
                ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
                actorRef.tell(taskExecuteDTO, actorRef);

            } catch (Exception e) {
                log.error("任务调度执行失败", e);
            } finally {
                // 处理常驻任务
                doHandlerResidentTask(job);

            }
        });
    }

    private void doHandlerResidentTask(Job job) {
        if (Objects.nonNull(job)) {
            // 是否是常驻任务
            if (Objects.equals(StatusEnum.YES.getStatus(), job.getResident())) {
                ResidentJobTimerTask timerTask = new ResidentJobTimerTask(jobTimerTaskDTO, job);
                WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(job.getTriggerType());

                LocalDateTime preTriggerAt = ResidentTaskCache.get(jobTimerTaskDTO.getJobId());
                if (Objects.isNull(preTriggerAt) || preTriggerAt.isBefore(job.getNextTriggerAt())) {
                    preTriggerAt = job.getNextTriggerAt();
                }

                WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
                waitStrategyContext.setTriggerType(job.getTriggerType());
                waitStrategyContext.setTriggerInterval(job.getTriggerInterval());
                waitStrategyContext.setNextTriggerAt(preTriggerAt);
                LocalDateTime nextTriggerAt = waitStrategy.computeRetryTime(waitStrategyContext);

                // 获取时间差的毫秒数
                Duration duration = Duration.between(preTriggerAt, nextTriggerAt);
                long milliseconds = duration.toMillis();

                log.info("常驻任务监控. 任务时间差:[{}] 取余:[{}]", milliseconds, System.currentTimeMillis() % 1000);
                job.setNextTriggerAt(nextTriggerAt);

                JobTimerWheel.register(jobTimerTaskDTO.getTaskBatchId(), timerTask, milliseconds - System.currentTimeMillis() % 1000, TimeUnit.MILLISECONDS);
                ResidentTaskCache.refresh(jobTimerTaskDTO.getJobId(), nextTriggerAt);
            }
        }
    }
}
