package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;
import com.aizuda.easy.retry.server.job.task.support.JobExecutor;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackFactory;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.easy.retry.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.server.web.service.WorkflowNodeService;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2024-02-03 21:25:00
 * @since 2.6.0
 */
@Service
@RequiredArgsConstructor
public class WorkflowNodeServiceImpl implements WorkflowNodeService {
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobMapper jobMapper;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobTaskMapper jobTaskMapper;

    @Override
    public Boolean stop(Long nodeId, Long workflowTaskBatchId) {
        // 调用JOB的停止接口
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getWorkflowNodeId, nodeId)
                        .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_COMPLETE)
        );

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return Boolean.TRUE;
        }

        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {

            Job job = jobMapper.selectById(jobTaskBatch.getJobId());
            Assert.notNull(job, () -> new EasyRetryServerException("job can not be null."));

            JobTaskStopHandler jobTaskStop = JobTaskStopFactory.getJobTaskStop(job.getTaskType());

            TaskStopJobContext taskStopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
            taskStopJobContext.setJobOperationReason(JobOperationReasonEnum.MANNER_STOP.getReason());
            taskStopJobContext.setTaskBatchId(jobTaskBatch.getId());
            taskStopJobContext.setForceStop(Boolean.TRUE);
            taskStopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);

            jobTaskStop.stop(taskStopJobContext);
        }

        // 继续执行后续的任务
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
        taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_WORKFLOW.getType());
        taskExecuteDTO.setParentId(nodeId);

        workflowBatchHandler.openNextNode(taskExecuteDTO);

        return Boolean.TRUE;
    }

    @Override
    public Boolean retry(Long nodeId, Long workflowTaskBatchId) {

        // 调用JOB的停止接口
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .select(JobTaskBatch::getId)
                        .eq(JobTaskBatch::getWorkflowNodeId, nodeId)
                        .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId)
                        .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.NOT_SUCCESS)
        );
        Assert.notEmpty(jobTaskBatches, () -> new EasyRetryServerException("job task batch is empty."));

        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {

            Job job = jobMapper.selectById(jobTaskBatch.getJobId());
            Assert.notNull(job, () -> new EasyRetryServerException("job can not be null."));

            List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                    .select(JobTask::getId)
                    .eq(JobTask::getTaskBatchId, jobTaskBatch.getId()));
            Assert.notEmpty(jobTasks, () -> new EasyRetryServerException("job task is empty."));

            for (JobTask jobTask : jobTasks) {
                // 模拟失败重试
                ClientCallbackHandler clientCallback = ClientCallbackFactory.getClientCallback(job.getTaskType());
                ClientCallbackContext context = JobTaskConverter.INSTANCE.toClientCallbackContext(job);
                context.setTaskBatchId(jobTaskBatch.getId());
                context.setTaskId(jobTask.getId());
                context.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
                context.setExecuteResult(ExecuteResult.failure(null, "手动重试"));
                clientCallback.callback(context);
            }

//            JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(job.getTaskType());
//
//            JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
//            context.setTaskList(jobTaRFGVTBCD67YFGVTBUE8SDsks);
//            context.setTaskBatchId(jobTaskBatch.getId());
//            context.setWorkflowTaskBatchId(workflowTaskBatchId);
//            context.setWorkflowNodeId(nodeId);
//            jobExecutor.execute(context);

        }

        return Boolean.TRUE;
    }
}
