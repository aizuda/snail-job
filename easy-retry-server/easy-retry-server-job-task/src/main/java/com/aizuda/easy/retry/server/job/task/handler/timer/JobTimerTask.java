package com.aizuda.easy.retry.server.job.task.handler.timer;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.easy.retry.server.job.task.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>());

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        log.info("开始执行任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), jobTimerTaskDTO.getTaskBatchId());

        executor.execute(() -> {

            try {
                // 清除时间轮的缓存
                JobTimerWheelHandler.clearCache(jobTimerTaskDTO.getGroupName(), jobTimerTaskDTO.getTaskBatchId());

                JobMapper jobMapper = SpringContext.getBeanByType(JobMapper.class);
                Job job = jobMapper.selectOne(new LambdaQueryWrapper<Job>()
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
                jobTaskBatch.setTaskStatus(taskStatus);
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
            }

        });

    }
}
