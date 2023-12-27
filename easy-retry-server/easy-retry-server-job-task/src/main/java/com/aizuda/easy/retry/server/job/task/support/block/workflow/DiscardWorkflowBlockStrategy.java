package com.aizuda.easy.retry.server.job.task.support.block.workflow;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.constant.SystemConstants;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.block.job.BlockStrategies.BlockStrategyEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: xiaowoniu
 * @date : 2023-12-26
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class DiscardWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {
    private final WorkflowBatchGenerator workflowBatchGenerator;
    @Override
    protected void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext) {

        // 重新尝试执行, 重新生成任务批次
        WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
        taskExecuteDTO.setWorkflowTaskBatchId(workflowBlockStrategyContext.getWorkflowTaskBatchId());
        taskExecuteDTO.setWorkflowId(workflowBlockStrategyContext.getWorkflowId());
        taskExecuteDTO.setTriggerType(workflowBlockStrategyContext.getTriggerType());
        taskExecuteDTO.setParentId(SystemConstants.ROOT);
        ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
        actorRef.tell(taskExecuteDTO, actorRef);

        // 生成状态为取消的工作流批次
        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(workflowBlockStrategyContext);
        workflowTaskBatchGeneratorContext.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
        workflowTaskBatchGeneratorContext.setOperationReason(JobOperationReasonEnum.JOB_DISCARD.getReason());
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.DISCARD;
    }
}
