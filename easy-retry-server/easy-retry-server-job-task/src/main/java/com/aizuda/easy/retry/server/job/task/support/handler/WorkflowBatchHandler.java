package com.aizuda.easy.retry.server.job.task.support.handler;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.common.util.GraphUtils;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum.COMPLETED;
import static com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum.NOT_COMPLETE;

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
        workflowTaskBatch = Optional.ofNullable(workflowTaskBatch)
            .orElseGet(() -> workflowTaskBatchMapper.selectById(workflowTaskBatchId));
        Assert.notNull(workflowTaskBatch, () -> new EasyRetryServerException("任务不存在"));

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = GraphUtils.deserializeJsonToGraph(flowInfo);

        // 说明没有后继节点了, 此时需要判断整个DAG是否全部执行完成
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
            .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatch.getId())
            .in(JobTaskBatch::getWorkflowNodeId, graph.nodes())
        );

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return false;
        }

        if (jobTaskBatches.stream().anyMatch(
            jobTaskBatch -> JobTaskBatchStatusEnum.NOT_COMPLETE.contains(jobTaskBatch.getTaskBatchStatus()))) {
            return false;
        }

        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
            .in(WorkflowNode::getId, graph.nodes()));
        if (jobTaskBatches.size() < workflowNodes.size()) {
            return false;
        }

        Map<Long, WorkflowNode> workflowNodeMap = workflowNodes.stream()
            .collect(Collectors.toMap(WorkflowNode::getId, workflowNode -> workflowNode));

        Map<Long, List<JobTaskBatch>> map = jobTaskBatches.stream()
            .collect(Collectors.groupingBy(JobTaskBatch::getParentWorkflowNodeId));

        int taskStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
        int operationReason = JobOperationReasonEnum.NONE.getReason();
        for (final JobTaskBatch jobTaskBatch : jobTaskBatches) {
            Set<Long> predecessors = graph.predecessors(jobTaskBatch.getWorkflowNodeId());
            WorkflowNode workflowNode = workflowNodeMap.get(jobTaskBatch.getWorkflowNodeId());
            // 条件节点是或的关系一个成功就代表成功
            if (WorkflowNodeTypeEnum.CONDITION.getType() == workflowNode.getNodeType()) {
                for (final Long predecessor : predecessors) {
                    List<JobTaskBatch> jobTaskBatcheList = map.get(predecessor);
                    Map<Integer, Long> statusCountMap = jobTaskBatcheList.stream()
                        .collect(Collectors.groupingBy(JobTaskBatch::getTaskBatchStatus, Collectors.counting()));
                    long successCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.SUCCESS.getStatus(), 0L);
                    long failCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.FAIL.getStatus(), 0L);
                    long stopCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.STOP.getStatus(), 0L);
                    if (successCount > 0) {
                        break;
                    }

                    if (failCount > 0) {
                        taskStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
                        operationReason = JobOperationReasonEnum.TASK_EXECUTE_ERROR.getReason();
                        break;
                    }

                    if (stopCount > 0) {
                        taskStatus = JobTaskBatchStatusEnum.STOP.getStatus();
                        operationReason = JobOperationReasonEnum.TASK_EXECUTE_ERROR.getReason();
                        break;
                    }

                }
            } else {

                for (final Long predecessor : predecessors) {
                    List<JobTaskBatch> jobTaskBatcheList = map.get(predecessor);
                    Map<Integer, Long> statusCountMap = jobTaskBatcheList.stream()
                       .collect(Collectors.groupingBy(JobTaskBatch::getTaskBatchStatus, Collectors.counting()));
                    long failCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.FAIL.getStatus(), 0L);
                    long stopCount = statusCountMap.getOrDefault(JobTaskBatchStatusEnum.STOP.getStatus(), 0L);                    if (failCount > 0) {
                        taskStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
                        operationReason = JobOperationReasonEnum.TASK_EXECUTE_ERROR.getReason();
                        break;
                    }

                    if (stopCount > 0) {
                        taskStatus = JobTaskBatchStatusEnum.STOP.getStatus();
                        operationReason = JobOperationReasonEnum.TASK_EXECUTE_ERROR.getReason();
                        break;
                    }
                }
            }


            if (taskStatus != JobTaskBatchStatusEnum.SUCCESS.getStatus()) {
                break;
            }

        }

        handlerTaskBatch(workflowTaskBatchId, taskStatus, operationReason);

        return true;

    }

    private void handlerTaskBatch(Long workflowTaskBatchId, int taskStatus, int operationReason) {

        WorkflowTaskBatch jobTaskBatch = new WorkflowTaskBatch();
        jobTaskBatch.setId(workflowTaskBatchId);
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        Assert.isTrue(1 == workflowTaskBatchMapper.updateById(jobTaskBatch),
            () -> new EasyRetryServerException("更新任务失败"));

    }

    public void stop(Long workflowTaskBatchId, Integer operationReason) {
        if (Objects.isNull(operationReason)
            || operationReason == JobOperationReasonEnum.NONE.getReason()) {
            operationReason = JobOperationReasonEnum.JOB_OVERLAY.getReason();
        }

        WorkflowTaskBatch workflowTaskBatch = new WorkflowTaskBatch();
        workflowTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.STOP.getStatus());
        workflowTaskBatch.setOperationReason(operationReason);
        workflowTaskBatch.setId(workflowTaskBatchId);
        // 先停止执行中的批次
        Assert.isTrue(1 == workflowTaskBatchMapper.updateById(workflowTaskBatch),
            () -> new EasyRetryServerException("停止工作流批次失败. id:[{}]",
                workflowTaskBatchId));

        // 关闭已经触发的任务
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
            .in(JobTaskBatch::getTaskBatchStatus, NOT_COMPLETE)
            .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId));

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return;
        }

        List<Job> jobs = jobMapper.selectBatchIds(
            jobTaskBatches.stream().map(JobTaskBatch::getJobId).collect(Collectors.toSet()));

        Map<Long, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, i -> i));
        for (final JobTaskBatch jobTaskBatch : jobTaskBatches) {

            Job job = jobMap.get(jobTaskBatch.getJobId());
            if (Objects.nonNull(job)) {
                // 停止任务
                JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(job.getTaskType());
                TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(job);
                stopJobContext.setTaskBatchId(jobTaskBatch.getId());
                stopJobContext.setJobOperationReason(JobOperationReasonEnum.JOB_TASK_INTERRUPTED.getReason());
                stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
                stopJobContext.setForceStop(Boolean.TRUE);
                instanceInterrupt.stop(stopJobContext);
            }

        }
    }
}
