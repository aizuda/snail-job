package com.aizuda.easy.retry.server.web.service.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public Boolean retry (Long taskBatchId, Long workflowNodeId, Long workflowTaskBatchId) {
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

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
            .in(JobTask::getTaskStatus, Lists.newArrayList(
                    JobTaskStatusEnum.FAIL.getStatus(),
                    JobTaskStatusEnum.STOP.getStatus(),
                    JobTaskStatusEnum.CANCEL.getStatus()
                )
            )
            .eq(JobTask::getTaskBatchId, taskBatchId));
        Assert.notEmpty(jobTasks, () -> new EasyRetryServerException("job task is empty."));

        for (JobTask jobTask : jobTasks) {
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
            context.setExecuteResult(ExecuteResult.failure(null, "手动重试"));
            clientCallback.callback(context);
        }

        return Boolean.TRUE;
    }

    public Boolean stop (Long taskBatchId) {

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
