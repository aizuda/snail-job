package com.aizuda.easy.retry.server.job.task.support.block.workflow;

import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
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
public class OverlayWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {

    private final WorkflowBatchHandler workflowBatchHandler;
    private final WorkflowBatchGenerator workflowBatchGenerator;

    @Override
    protected void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext) {

        // 停止当前批次
        workflowBatchHandler.stop(workflowBlockStrategyContext.getWorkflowTaskBatchId(), workflowBlockStrategyContext.getOperationReason());

        // 重新生成一个批次
        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(
            workflowBlockStrategyContext);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);

    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.OVERLAY;
    }
}
