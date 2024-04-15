package com.aizuda.snail.job.server.web.service.handler;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.snail.job.client.model.ExecuteResult;
import com.aizuda.snail.job.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snail.job.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snail.job.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snail.job.server.common.akka.ActorGenerator;
import com.aizuda.snail.job.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import com.aizuda.snail.job.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.snail.job.server.job.task.enums.JobRetrySceneEnum;
import com.aizuda.snail.job.server.job.task.support.ClientCallbackHandler;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import com.aizuda.snail.job.server.job.task.support.JobTaskStopHandler;
import com.aizuda.snail.job.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.snail.job.server.job.task.support.callback.ClientCallbackFactory;
import com.aizuda.snail.job.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.snail.job.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snail.job.template.datasource.persistence.po.Job;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTask;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2024-02-26
 * @since : 3.1.0
 */
@Component
@RequiredArgsConstructor
public class JobHandler {

    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobMapper jobMapper;
    private final JobTaskMapper jobTaskMapper;

    public Boolean retry(Long taskBatchId) {
        return retry(taskBatchId, null, null);
    }

    public Boolean retry(Long taskBatchId, Long workflowNodeId, Long workflowTaskBatchId) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectOne(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getId, taskBatchId)
                .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_SUCCESS)
        );
        Assert.notNull(jobTaskBatch, () -> new EasyRetryServerException("job batch can not be null."));

        // 重置状态为运行中
        jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.RUNNING.getStatus());

        Assert.isTrue(jobTaskBatchMapper.updateById(jobTaskBatch) > 0,
                () -> new EasyRetryServerException("update job batch to running failed."));

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        Assert.notNull(job, () -> new EasyRetryServerException("job can not be null."));

        List<JobTask> jobTasks = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getId, JobTask::getTaskStatus)
                        .eq(JobTask::getTaskBatchId, taskBatchId));

        //  若任务项为空则生成
        if (CollectionUtils.isEmpty(jobTasks)) {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(taskBatchId);
            taskExecuteDTO.setJobId(jobTaskBatch.getJobId());
            taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_JOB.getType());
            taskExecuteDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
            taskExecuteDTO.setWorkflowNodeId(workflowNodeId);
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

            return Boolean.TRUE;
        }

        for (JobTask jobTask : jobTasks) {
            if (jobTask.getTaskStatus() == JobTaskStatusEnum.RUNNING.getStatus()) {
                continue;
            }

            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            Assert.isTrue(jobTaskMapper.updateById(jobTask) > 0,
                    () -> new EasyRetryServerException("update job task to running failed."));
            // 模拟失败重试
            ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(job.getTaskType());
            ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(job);
            context.setTaskBatchId(jobTaskBatch.getId());
            context.setWorkflowNodeId(workflowNodeId);
            context.setWorkflowTaskBatchId(workflowTaskBatchId);
            context.setTaskId(jobTask.getId());
            context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
            context.setRetryScene(JobRetrySceneEnum.MANUAL.getRetryScene());
            context.setExecuteResult(ExecuteResult.failure(null, "手动重试"));
            clientCallback.callback(context);
        }

        return Boolean.TRUE;
    }

    public Boolean stop(Long taskBatchId) {

        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(taskBatchId);
        Assert.notNull(jobTaskBatch, () -> new EasyRetryServerException("job batch can not be null."));

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        Assert.notNull(job, () -> new EasyRetryServerException("job can not be null."));

        JobTaskStopHandler jobTaskStop = JobTaskStopFactory.getJobTaskStop(job.getTaskType());

        TaskStopJobContext taskStopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
        taskStopJobContext.setJobOperationReason(JobOperationReasonEnum.MANNER_STOP.getReason());
        taskStopJobContext.setTaskBatchId(jobTaskBatch.getId());
        taskStopJobContext.setForceStop(Boolean.TRUE);
        taskStopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);

        jobTaskStop.stop(taskStopJobContext);

        return Boolean.TRUE;
    }

}
