package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
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

    private final JobTaskBatchGenerator jobTaskBatchGenerator;

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

        if (Objects.equals(context.getWorkflowNodeStatus(), StatusEnum.NO.getStatus())) {
            JobTaskBatchGeneratorContext generatorContext = WorkflowTaskConverter.INSTANCE.toJobTaskBatchGeneratorContext(context);
            generatorContext.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            generatorContext.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_CLOSED_SKIP_EXECUTION.getReason());
            generatorContext.setJobId(context.getJobId());
            generatorContext.setTaskExecutorScene(context.getTaskExecutorScene());
            jobTaskBatchGenerator.generateJobTaskBatch(generatorContext);
            workflowBatchHandler.complete(context.getWorkflowTaskBatchId());

            // 执行下一个节点
            workflowTaskExecutor(context);
        } else {
            invokeJobTask(context);
        }

    }

    private static void invokeJobTask(final WorkflowExecutorContext context) {
        // 生成任务批次
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(context.getJob(), context);
//        jobTaskPrepare.setTaskExecutorScene(context.getTaskExecutorScene());
        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli() + DateUtils.toNowMilli() % 1000);
//        jobTaskPrepare.setWorkflowNodeId(context.getWorkflowNodeId());
//        jobTaskPrepare.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
//        jobTaskPrepare.setParentWorkflowNodeId(context.getParentWorkflowNodeId());
        // 执行预处理阶段
        ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
        actorRef.tell(jobTaskPrepare, actorRef);
    }
}
