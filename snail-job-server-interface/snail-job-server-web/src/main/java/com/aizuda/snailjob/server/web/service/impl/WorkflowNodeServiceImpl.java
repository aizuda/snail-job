package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.web.service.WorkflowNodeService;
import com.aizuda.snailjob.server.web.service.handler.JobHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (CollUtil.isEmpty(jobTaskBatches)) {
            return Boolean.TRUE;
        }

        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {
            jobHandler.stop(jobTaskBatch.getId());
        }

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

        Assert.notEmpty(jobTaskBatches, () -> new SnailJobServerException("job task batch is empty."));

        for (JobTaskBatch jobTaskBatch : jobTaskBatches) {
            jobHandler.retry(jobTaskBatch.getId(), nodeId, workflowTaskBatchId);
        }

        return Boolean.TRUE;
    }
}
