package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobLogMetaDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        if (Objects.equals(context.getWorkflowNodeStatus(), StatusEnum.YES.getStatus())) {
            return;
        }

        JobTaskBatch jobTaskBatch = generateJobTaskBatch(context);
        JobTask jobTask = generateJobTask(context, jobTaskBatch);

        JobLogMetaDTO jobLogMetaDTO = new JobLogMetaDTO();
        jobLogMetaDTO.setNamespaceId(context.getNamespaceId());
        jobLogMetaDTO.setGroupName(context.getGroupName());
        jobLogMetaDTO.setTaskBatchId(jobTaskBatch.getId());
        jobLogMetaDTO.setJobId(context.getJobId());
        jobLogMetaDTO.setTaskId(jobTask.getId());

        EasyRetryLog.REMOTE.warn("节点[{}]已取消任务执行. 取消原因: 任务已关闭. <|>{}<|>",
                context.getWorkflowNodeId(), jobLogMetaDTO);
    }

    @Override
    protected void beforeExecute(WorkflowExecutorContext context) {

    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

        if (Objects.equals(context.getWorkflowNodeStatus(), StatusEnum.NO.getStatus())) {
            context.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            context.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.getReason());
            context.setJobTaskStatus(JobTaskStatusEnum.CANCEL.getStatus());

            // 执行下一个节点
            workflowTaskExecutor(context);
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
}
