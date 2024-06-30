package com.aizuda.snailjob.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SpringContext;
import com.aizuda.snailjob.common.core.enums.FailStrategyEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.support.WorkflowExecutor;
import com.aizuda.snailjob.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snailjob.server.job.task.support.alarm.event.WorkflowTaskFailAlarmEvent;
import com.aizuda.snailjob.server.job.task.support.cache.MutableGraphCache;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorFactory;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.WorkflowTimeoutCheckTask;
import com.aizuda.snailjob.server.job.task.support.timer.WorkflowTimerTask;
import com.aizuda.snailjob.template.datasource.persistence.mapper.*;
import com.aizuda.snailjob.template.datasource.persistence.po.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22 10:34
 * @since : 2.6.0
 */
@Component(ActorGenerator.WORKFLOW_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor
public class WorkflowExecutorActor extends AbstractActor {

    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    private final WorkflowMapper workflowMapper;
    private final JobMapper jobMapper;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final WorkflowBatchHandler workflowBatchHandler;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(WorkflowNodeTaskExecuteDTO.class, taskExecute -> {
            log.info("工作流开始执行. [{}]", JsonUtil.toJsonString(taskExecute));
            try {

                doExecutor(taskExecute);

            } catch (Exception e) {
                SnailJobLog.LOCAL.error("workflow executor exception. [{}]", taskExecute, e);
                handlerTaskBatch(taskExecute, JobTaskBatchStatusEnum.FAIL.getStatus(),
                    JobOperationReasonEnum.TASK_EXECUTION_ERROR.getReason());
                SpringContext.getContext()
                    .publishEvent(new WorkflowTaskFailAlarmEvent(taskExecute.getWorkflowTaskBatchId()));
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doExecutor(WorkflowNodeTaskExecuteDTO taskExecute) {
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectById(taskExecute.getWorkflowTaskBatchId());
        Assert.notNull(workflowTaskBatch, () -> new SnailJobServerException("任务不存在"));

        if (SystemConstants.ROOT.equals(taskExecute.getParentId())
            && JobTaskBatchStatusEnum.WAITING.getStatus() == workflowTaskBatch.getTaskBatchStatus()) {
            handlerTaskBatch(taskExecute, JobTaskBatchStatusEnum.RUNNING.getStatus(),
                JobOperationReasonEnum.NONE.getReason());

            Workflow workflow = workflowMapper.selectById(workflowTaskBatch.getWorkflowId());
            JobTimerWheel.clearCache(
                MessageFormat.format(WorkflowTimerTask.IDEMPOTENT_KEY_PREFIX, taskExecute.getWorkflowTaskBatchId()));

            JobTimerWheel.registerWithWorkflow(() -> new WorkflowTimeoutCheckTask(taskExecute.getWorkflowTaskBatchId()),
                Duration.ofSeconds(workflow.getExecutorTimeout()));
        }

        // 获取DAG图
        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<Long> graph = MutableGraphCache.getOrDefault(workflowTaskBatch.getId(), flowInfo);

        Set<Long> brotherNode = MutableGraphCache.getBrotherNode(graph, taskExecute.getParentId());
        Sets.SetView<Long> setView = Sets.union(brotherNode, Sets.newHashSet(taskExecute.getParentId()));
        // 查到当前节点【ParentId】的所有兄弟节点是否有后继节点，若有则不能直接完成任务
        Set<Long> allSuccessors = Sets.newHashSet();
        for (Long nodeId : setView.immutableCopy()) {
            Set<Long> successors = graph.successors(nodeId);
            if (CollUtil.isNotEmpty(successors)) {
                for (final Long successor : successors) {
                    // 寻找当前的节点的所有前序节点
                    allSuccessors.addAll(graph.predecessors(successor));
                }
                allSuccessors.addAll(successors);
            }
        }

        log.warn("父节点:[{}] 所有的节点:[{}]", taskExecute.getParentId(), allSuccessors);

        // 若所有的兄弟节点的子节点都没有后继节点可以完成次任务
        if (CollUtil.isEmpty(allSuccessors)) {
            workflowBatchHandler.complete(taskExecute.getWorkflowTaskBatchId(), workflowTaskBatch);
            return;
        }

        // 添加父节点，为了判断父节点的处理状态
        List<JobTaskBatch> allJobTaskBatchList = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>()
            .select(JobTaskBatch::getWorkflowTaskBatchId, JobTaskBatch::getWorkflowNodeId,
                JobTaskBatch::getTaskBatchStatus, JobTaskBatch::getOperationReason, JobTaskBatch::getId)
            .eq(JobTaskBatch::getWorkflowTaskBatchId, workflowTaskBatch.getId())
            .in(JobTaskBatch::getWorkflowNodeId,
                Sets.union(allSuccessors, Sets.newHashSet(taskExecute.getParentId())))
        );

        List<WorkflowNode> workflowNodes = workflowNodeMapper.selectList(new LambdaQueryWrapper<WorkflowNode>()
            .in(WorkflowNode::getId, Sets.union(allSuccessors, Sets.newHashSet(taskExecute.getParentId())))
            .orderByAsc(WorkflowNode::getPriorityLevel));

        Map<Long, List<JobTaskBatch>> jobTaskBatchMap = StreamUtils.groupByKey(allJobTaskBatchList,
            JobTaskBatch::getWorkflowNodeId);
        Map<Long, WorkflowNode> workflowNodeMap = StreamUtils.toIdentityMap(workflowNodes, WorkflowNode::getId);
        List<JobTaskBatch> parentJobTaskBatchList = jobTaskBatchMap.get(taskExecute.getParentId());

        // 如果父节点是无需处理则不再继续执行
//        if (CollUtil.isNotEmpty(parentJobTaskBatchList) &&
//            parentJobTaskBatchList.stream()
//                .map(JobTaskBatch::getOperationReason)
//                .filter(Objects::nonNull)
//                .anyMatch(JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION::contains)) {
//            workflowBatchHandler.complete(taskExecute.getWorkflowTaskBatchId(), workflowTaskBatch);
//            return;
//        }

        WorkflowNode parentWorkflowNode = workflowNodeMap.get(taskExecute.getParentId());
        // 失败策略处理
        if (CollUtil.isNotEmpty(parentJobTaskBatchList)
            && parentJobTaskBatchList.stream()
                .map(JobTaskBatch::getTaskBatchStatus)
                .anyMatch(i -> i != JobTaskBatchStatusEnum.SUCCESS.getStatus())) {

            // 根据失败策略判断是否继续处理
            if (Objects.equals(parentWorkflowNode.getFailStrategy(), FailStrategyEnum.BLOCK.getCode())) {
                return;
            }
        }

        // 决策节点
        if (Objects.nonNull(parentWorkflowNode)
            && WorkflowNodeTypeEnum.DECISION.getType() == parentWorkflowNode.getNodeType()) {

            // 获取决策节点子节点
            Set<Long> successors = graph.successors(parentWorkflowNode.getId());
            workflowNodes = workflowNodes.stream()
                // 去掉父节点
                .filter(workflowNode -> !workflowNode.getId().equals(taskExecute.getParentId())
                                        // 过滤掉非当前决策节点【ParentId】的子节点
                                        && successors.contains(workflowNode.getId())).collect(Collectors.toList());
        } else {
            // TODO 不通过兄弟节点去控制是否执行后续节点
//            if (!brotherNodeIsComplete(taskExecute, brotherNode, jobTaskBatchMap, parentWorkflowNode)) {
//                return;
//            }

            workflowNodes = workflowNodes.stream()
                // 去掉父节点
                .filter(workflowNode -> !workflowNode.getId().equals(taskExecute.getParentId()))
                .collect(Collectors.toList());

            // TODO 合并job task的结果到全局上下文中
            // 此次的并发数与当时父节点的兄弟节点的数量一致
            workflowBatchHandler.mergeWorkflowContextAndRetry(workflowTaskBatch,
                StreamUtils.toSet(allJobTaskBatchList, JobTaskBatch::getId));
        }

        List<Job> jobs = jobMapper.selectBatchIds(StreamUtils.toSet(workflowNodes, WorkflowNode::getJobId));
        Map<Long, Job> jobMap = StreamUtils.toIdentityMap(jobs, Job::getId);

        // 只会条件节点会使用
        Object evaluationResult = null;
        log.info("待执行的节点为. workflowNodes:[{}]", StreamUtils.toList(workflowNodes, WorkflowNode::getId));
        for (WorkflowNode workflowNode : workflowNodes) {

            // 批次已经存在就不在重复生成
            List<JobTaskBatch> jobTaskBatchList = jobTaskBatchMap.get(workflowNode.getId());
            if (CollUtil.isNotEmpty(jobTaskBatchList)) {
                continue;
            }

            // 决策当前节点要不要执行
            Set<Long> predecessors = graph.predecessors(workflowNode.getId());
            boolean predecessorsComplete = arePredecessorsComplete(taskExecute, predecessors, jobTaskBatchMap, workflowNode, parentJobTaskBatchList);
            if (!SystemConstants.ROOT.equals(taskExecute.getParentId()) && !predecessorsComplete) {
                continue;
            }

            // 执行DAG中的节点
            WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getWorkflowExecutor(workflowNode.getNodeType());

            WorkflowExecutorContext context = WorkflowTaskConverter.INSTANCE.toWorkflowExecutorContext(workflowNode);
            context.setJob(jobMap.get(workflowNode.getJobId()));
            context.setWorkflowTaskBatchId(taskExecute.getWorkflowTaskBatchId());
            context.setParentWorkflowNodeId(taskExecute.getParentId());
            context.setEvaluationResult(evaluationResult);
            context.setTaskBatchId(taskExecute.getTaskBatchId());
            context.setTaskExecutorScene(taskExecute.getTaskExecutorScene());
            context.setWfContext(workflowTaskBatch.getWfContext());
            // 这里父节点取最新的批次判断状态
            if (CollUtil.isNotEmpty(parentJobTaskBatchList)) {
                context.setParentOperationReason(parentJobTaskBatchList.get(0).getOperationReason());
            }

            workflowExecutor.execute(context);

            evaluationResult = context.getEvaluationResult();
        }

    }

    private boolean arePredecessorsComplete(final WorkflowNodeTaskExecuteDTO taskExecute, Set<Long> predecessors,
        Map<Long, List<JobTaskBatch>> jobTaskBatchMap, WorkflowNode waitExecWorkflowNode,
        List<JobTaskBatch> parentJobTaskBatchList) {

        // 是否存在无需处理的节点
        List<JobTaskBatch> isExistedNotSkipJobTaskBatches = new ArrayList<>();
        // 判断所有节点是否都完成
        for (final Long nodeId : predecessors) {
            if (SystemConstants.ROOT.equals(nodeId)) {
                continue;
            }

            List<JobTaskBatch> jobTaskBatches = jobTaskBatchMap.get(nodeId);
            // 说明此节点未执行, 继续等待执行完成
            if (CollUtil.isEmpty(jobTaskBatches)) {
                SnailJobLog.LOCAL.info("批次为空存在未完成的兄弟节点. [{}] 待执行节点:[{}]", nodeId,
                    waitExecWorkflowNode.getId());
                return Boolean.FALSE;
            }

            boolean isCompleted = jobTaskBatches.stream().anyMatch(
                jobTaskBatch -> JobTaskBatchStatusEnum.NOT_COMPLETE.contains(jobTaskBatch.getTaskBatchStatus()));
            if (isCompleted) {
                SnailJobLog.LOCAL.info("存在未完成的兄弟节点. [{}] 待执行节点:[{}] parentId:[{}]", nodeId, taskExecute.getParentId(),
                    waitExecWorkflowNode.getId());
                return Boolean.FALSE;
            }

            if (CollUtil.isEmpty(isExistedNotSkipJobTaskBatches)) {
                isExistedNotSkipJobTaskBatches = jobTaskBatches.stream().filter(
                    jobTaskBatch -> !WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(jobTaskBatch.getOperationReason())).toList();

            }

        }

        // 父节点是否存在无需处理的节点，若存在一个不是的则需要等待正常的节点处理
        // 如果父节点是无需处理则不再继续执行
        if (CollUtil.isNotEmpty(parentJobTaskBatchList) &&
            parentJobTaskBatchList.stream()
                .map(JobTaskBatch::getOperationReason)
                .filter(Objects::nonNull)
                .anyMatch(JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION::contains)
            && CollUtil.isNotEmpty(isExistedNotSkipJobTaskBatches)) {
            /*
             等待正常的节点来执行此节点，若正常节点已经调度过了，此时则没有能触发后继节点继续调度的节点存在了。
             因此这里将重新选当前节点的前驱节点中选一个作为父节点来触发，使之能够继续往后执行
             基于性能的考虑这里在直接在parentJobTaskBatchList列表的头节点插入一个不是跳过的节点，这样就可以正常流转了
             eg: {"-1":[480],"480":[481,488,490],"481":[482],"482":[483],"483":[484],"484":[485],"485":[486],"486":[487],"487":[497,498],"488":[489],"489":[497,498],"490":[491,493,495],"491":[492],"492":[497,498],"493":[494],"494":[497,498],"495":[496],"496":[497,498],"497":[499],"498":[499],"499":[]}
            */
            log.warn("-->>> isExistedNotSkip:[{}] nodeId:[{}] parentId:[{}]",  CollUtil.isNotEmpty(isExistedNotSkipJobTaskBatches), waitExecWorkflowNode.getId(), taskExecute.getParentId());
            parentJobTaskBatchList.add(0, isExistedNotSkipJobTaskBatches.get(0));
        }

        return Boolean.TRUE;
    }

    private void handlerTaskBatch(WorkflowNodeTaskExecuteDTO taskExecute, int taskStatus, int operationReason) {

        WorkflowTaskBatch jobTaskBatch = new WorkflowTaskBatch();
        jobTaskBatch.setId(taskExecute.getWorkflowTaskBatchId());
        jobTaskBatch.setExecutionAt(DateUtils.toNowMilli());
        jobTaskBatch.setTaskBatchStatus(taskStatus);
        jobTaskBatch.setOperationReason(operationReason);
        jobTaskBatch.setUpdateDt(LocalDateTime.now());
        Assert.isTrue(1 == workflowTaskBatchMapper.updateById(jobTaskBatch),
            () -> new SnailJobServerException("更新任务失败"));

    }

}
