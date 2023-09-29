package com.aizuda.easy.retry.server.job.task.handler.timer;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
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
            Long jobId = 0L;
            String groupName = "";
            try {
                JobTaskBatchMapper jobTaskBatchMapper = SpringContext.getBeanByType(JobTaskBatchMapper.class);
                JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                    .select(JobTaskBatch::getJobId, JobTaskBatch::getGroupName, JobTaskBatch::getId)
                    .eq(JobTaskBatch::getId, jobTimerTaskDTO.getTaskBatchId())
                    .eq(JobTaskBatch::getTaskStatus, JobTaskBatchStatusEnum.WAITING.getStatus()));
                if (Objects.isNull(jobTaskBatch)) {
                    return;
                }

                jobId = jobTaskBatch.getJobId();
                groupName = jobTaskBatch.getGroupName();
                jobTaskBatch.setExecutionAt(LocalDateTime.now());
                jobTaskBatch.setTaskStatus(JobTaskBatchStatusEnum.RUNNING.getStatus());
                Assert.isTrue(1 == jobTaskBatchMapper.updateById(jobTaskBatch),
                    () -> new EasyRetryServerException("更新任务失败"));

                TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
                taskExecuteDTO.setTaskBatchId(jobTimerTaskDTO.getTaskBatchId());
                taskExecuteDTO.setGroupName(groupName);
                taskExecuteDTO.setJobId(jobId);
                ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
                actorRef.tell(taskExecuteDTO, actorRef);
            } catch (Exception e) {
                log.error("任务调度执行失败", e);
            } finally {
                // 清除时间轮的缓存
                JobTimerWheelHandler.clearCache(groupName, jobId);
            }

        });

    }
}
