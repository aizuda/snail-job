package com.aizuda.easy.retry.server.job.task.support.block.workflow;

import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.block.job.BlockStrategies.BlockStrategyEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: shuguang.zhang
 * @date : 2023-12-26
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class ConcurrencyWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {
    private final WorkflowBatchGenerator workflowBatchGenerator;

    @Override
    protected void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext) {
        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(workflowBlockStrategyContext);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.CONCURRENCY;
    }
}
