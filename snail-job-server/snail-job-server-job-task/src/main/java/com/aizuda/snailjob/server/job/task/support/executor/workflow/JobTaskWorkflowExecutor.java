package com.aizuda.snailjob.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.JobLogMetaDTO;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_CONDITION_NODE_EXECUTION_ERROR;
import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_DECISION_FAILED;
import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION;
import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED;
import static com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum.WORKFLOW_SUCCESSOR_SKIP_EXECUTION;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:09:14
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class JobTaskWorkflowExecutor extends AbstractWorkflowExecutor {


    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.JOB_TASK;
    }

    @Override
    protected boolean doPreValidate(WorkflowExecutorContext context) {
        return true;
    }

    @Override
    protected void afterExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

        if (WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(context.getParentOperationReason())) {
            // 针对无需处理的批次直接新增一个记录
            context.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.CANCEL.getStatus());

            // 创建批次和任务节点4
            invokeCancelJobTask(context, "当前节点无需处理");
        } else if (Objects.equals(context.getWorkflowNodeStatus(), StatusEnum.NO.getStatus())) {
            // 针对无需处理的批次直接新增一个记录
            context.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            context.setOperationReason(WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.CANCEL.getStatus());

            // 创建批次和任务节点
            invokeCancelJobTask(context, "任务已关闭");
        } else {
            invokeJobTask(context);
        }

    }

    private static void invokeJobTask(final WorkflowExecutorContext context) {
        // 生成任务批次
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(context.getJob(), context);
        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli() + DateUtils.toNowMilli() % 1000);
        // 执行预处理阶段
        ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
        actorRef.tell(jobTaskPrepare, actorRef);
    }

    private void invokeCancelJobTask(final WorkflowExecutorContext context, String cancelReason) {

        JobTaskBatch jobTaskBatch = generateJobTaskBatch(context);
        JobTask jobTask = generateJobTask(context, jobTaskBatch);

        JobLogMetaDTO jobLogMetaDTO = new JobLogMetaDTO();
        jobLogMetaDTO.setNamespaceId(context.getNamespaceId());
        jobLogMetaDTO.setGroupName(context.getGroupName());
        jobLogMetaDTO.setTaskBatchId(jobTaskBatch.getId());
        jobLogMetaDTO.setJobId(context.getJobId());
        jobLogMetaDTO.setTaskId(jobTask.getId());

        SnailJobLog.REMOTE.warn("节点[{}]已取消任务执行. 取消原因: {}. <|>{}<|>",
            context.getWorkflowNodeId(), cancelReason, jobLogMetaDTO);
    }
}
