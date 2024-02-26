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
import com.aizuda.easy.retry.server.web.service.handler.JobHandler;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final JobHandler jobHandler;

    @Override
    @Transactional
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
            jobHandler.stop(jobTaskBatch.getId());
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
    @Transactional
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
            jobHandler.retry(jobTaskBatch.getId(), nodeId, workflowTaskBatchId);
        }

        return Boolean.TRUE;
    }
}
