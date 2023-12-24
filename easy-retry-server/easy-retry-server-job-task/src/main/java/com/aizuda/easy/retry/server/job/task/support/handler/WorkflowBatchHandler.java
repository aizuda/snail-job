package com.aizuda.easy.retry.server.job.task.support.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.GraphUtils;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**
 * @author xiaowoniu
 * @date 2023-12-24 07:53:18
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class WorkflowBatchHandler {
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final JobMapper jobMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;

    public boolean complete(Long workflowTaskBatchId) throws IOException {
        return complete(workflowTaskBatchId, null);
    }
    public boolean complete(Long workflowTaskBatchId, WorkflowTaskBatch workflowTaskBatch) throws IOException {
        workflowTaskBatch = Optional.ofNullable(workflowTaskBatch).orElseGet(() -> workflowTaskBatchMapper.selectById(workflowTaskBatchId));
        Assert.notNull(workflowTaskBatch, () -> new EasyRetryServerException("任务不存在"));

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = GraphUtils.deserializeJsonToGraph(flowInfo);

        // 说明没有后继节点了, 此时需要判断整个DAG是否全部执行完成
        long executedTaskCount = jobTaskBatchMapper.selectCount(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatch.getId())
                .in(JobTaskBatch::getWorkflowNodeId, graph.nodes())
        );

        Long taskNodeCount = workflowNodeMapper.selectCount(new LambdaQueryWrapper<WorkflowNode>()
//                .eq(WorkflowNode::getNodeType, 1) // TODO 任务节点 若最后一个节点是条件或者是回调节点 这个地方就有问题
                .in(WorkflowNode::getId, graph.nodes()));

        // TODO 若最后几个节点都是非任务节点，这里直接完成就会有问题
        if (executedTaskCount < taskNodeCount) {
            return false;
        }

        handlerTaskBatch(workflowTaskBatchId, JobTaskBatchStatusEnum.SUCCESS.getStatus(), JobOperationReasonEnum.NONE.getReason());
        return true;

    }

    private void handlerTaskBatch(Long workflowTaskBatchId, int taskStatus, int operationReason) {

        WorkflowTaskBatch jobTaskBatch = new WorkflowTaskBatch();
        jobTaskBatch.setId(workflowTaskBatchId);
        jobTaskBatch.setExecutionAt(DateUtils.toNowMilli());
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        Assert.isTrue(1 == workflowTaskBatchMapper.updateById(jobTaskBatch),
                () -> new EasyRetryServerException("更新任务失败"));

    }
}
