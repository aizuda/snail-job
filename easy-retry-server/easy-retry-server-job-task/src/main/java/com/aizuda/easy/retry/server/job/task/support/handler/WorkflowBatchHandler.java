package com.aizuda.easy.retry.server.job.task.support.handler;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.cache.MutableGraphCache;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
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

import static com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;
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
    private final JobMapper jobMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;

    public boolean complete(Long workflowTaskBatchId) {
        return complete(workflowTaskBatchId, null);
    }

    public boolean complete(Long workflowTaskBatchId, WorkflowTaskBatch workflowTaskBatch) {
        workflowTaskBatch = Optional.ofNullable(workflowTaskBatch)
                .orElseGet(() -> workflowTaskBatchMapper.selectById(workflowTaskBatchId));
        Assert.notNull(workflowTaskBatch, () -> new EasyRetryServerException("任务不存在"));

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = MutableGraphCache.getOrDefault(workflowTaskBatchId, flowInfo);

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

        Map<Long, List<JobTaskBatch>> currentWorkflowNodeMap = jobTaskBatches.stream()
                .collect(Collectors.groupingBy(JobTaskBatch::getWorkflowNodeId));

        // 判定最后的工作流批次状态
        int taskStatus = JobTaskBatchStatusEnum.SUCCESS.getStatus();
        int operationReason = JobOperationReasonEnum.NONE.getReason();

        // 判定所有的叶子节点是否完成
        List<Long> leaves = MutableGraphCache.getLeaves(workflowTaskBatchId, flowInfo);
        for (Long leaf : leaves) {
            List<JobTaskBatch> jobTaskBatchList = currentWorkflowNodeMap.getOrDefault(leaf, Lists.newArrayList());
            if (CollectionUtils.isEmpty(jobTaskBatchList)) {
                boolean isNeedProcess = checkLeafCompleted(graph, currentWorkflowNodeMap, graph.predecessors(leaf));
                // 说明当前叶子节点需要处理，但是未处理返回false
                if (isNeedProcess) {
                    return false;
                }
            }

            // 判定叶子节点的状态
            for (JobTaskBatch jobTaskBatch : jobTaskBatchList) {
                if (JobTaskBatchStatusEnum.NOT_SUCCESS.contains(jobTaskBatch.getTaskBatchStatus())) {
                    // 只要叶子节点不是无需处理的都是失败
                    if (JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason() != jobTaskBatch.getOperationReason()
                            && JobOperationReasonEnum.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.getReason() != jobTaskBatch.getOperationReason()) {
                        taskStatus = JobTaskBatchStatusEnum.FAIL.getStatus();
                    }
                }
            }
        }

        handlerTaskBatch(workflowTaskBatchId, taskStatus, operationReason);

        return true;

    }

    private static boolean checkLeafCompleted(MutableGraph<Long> graph, Map<Long,
            List<JobTaskBatch>> currentWorkflowNodeMap, Set<Long> parentIds) {

        // 判定子节点是否需要处理
        boolean isNeedProcess = true;
        for (Long nodeId : parentIds) {
            List<JobTaskBatch> jobTaskBatchList = currentWorkflowNodeMap.get(nodeId);
            if (CollectionUtils.isEmpty(jobTaskBatchList)) {
                // 递归查询有执行过的任务批次
                isNeedProcess = isNeedProcess || checkLeafCompleted(graph, currentWorkflowNodeMap, graph.predecessors(nodeId));
                continue;
            }

            for (JobTaskBatch jobTaskBatch : jobTaskBatchList) {
                // 只要是无需处理的说明后面的子节点都不需要处理了，isNeedProcess为false
                if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())) {
                    isNeedProcess = false;
                    continue;
                }

                isNeedProcess = true;
            }

        }

        return isNeedProcess;
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

    public void checkWorkflowExecutor(Long workflowTaskBatchId, WorkflowTaskBatch workflowTaskBatch) throws IOException {
        workflowTaskBatch = Optional.ofNullable(workflowTaskBatch)
                .orElseGet(() -> workflowTaskBatchMapper.selectById(workflowTaskBatchId));
        Assert.notNull(workflowTaskBatch, () -> new EasyRetryServerException("任务不存在"));
        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = MutableGraphCache.getOrDefault(workflowTaskBatchId, flowInfo);
        Set<Long> successors = graph.successors(SystemConstants.ROOT);
        if (CollectionUtils.isEmpty(successors)) {
            return;
        }

        // 说明没有后继节点了, 此时需要判断整个DAG是否全部执行完成
        List<JobTaskBatch> jobTaskBatches = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatchId)
                .in(JobTaskBatch::getWorkflowNodeId, graph.nodes()).orderByDesc(JobTaskBatch::getId)
        );

        Map<Long, JobTaskBatch> jobTaskBatchMap = jobTaskBatches.stream().collect(Collectors.toMap(JobTaskBatch::getWorkflowNodeId, i -> i, (i,j) -> i));

        checkWorkflowExecutor(SystemConstants.ROOT, workflowTaskBatchId, graph, jobTaskBatchMap);
    }

    private void checkWorkflowExecutor(Long parentId, Long workflowTaskBatchId, MutableGraph<Long> graph, Map<Long, JobTaskBatch> jobTaskBatchMap) {

        // 判定条件节点是否已经执行完成
        JobTaskBatch parentJobTaskBatch = jobTaskBatchMap.get(parentId);
        if (Objects.nonNull(parentJobTaskBatch) &&
                WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(parentJobTaskBatch.getOperationReason())) {
            return;
        }

        Set<Long> successors = graph.successors(parentId);
        if (CollectionUtils.isEmpty(successors)) {
            return;
        }

        for (Long successor : successors) {
            JobTaskBatch jobTaskBatch = jobTaskBatchMap.get(successor);
            if (Objects.isNull(jobTaskBatch)) {
                // 重新尝试执行, 重新生成任务批次
                WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
                taskExecuteDTO.setWorkflowTaskBatchId(workflowTaskBatchId);
                taskExecuteDTO.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType());
                taskExecuteDTO.setParentId(parentId);
                ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
                actorRef.tell(taskExecuteDTO, actorRef);
                break;
            }

            if (NOT_COMPLETE.contains(jobTaskBatch.getTaskBatchStatus())) {
                // 生成任务批次
                Job job = jobMapper.selectById(jobTaskBatch.getJobId());
                JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
                jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_WORKFLOW.getType());
                jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli() + DateUtils.toNowMilli() % 1000);
                jobTaskPrepare.setWorkflowTaskBatchId(workflowTaskBatchId);
                jobTaskPrepare.setWorkflowNodeId(successor);
                jobTaskPrepare.setParentWorkflowNodeId(parentId);
                // 执行预处理阶段
                ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
                actorRef.tell(jobTaskPrepare, actorRef);
                break;
            }

            // 已经是终态的需要递归遍历后继节点是否正常执行
            checkWorkflowExecutor(successor, workflowTaskBatchId, graph, jobTaskBatchMap);
        }
    }


}
