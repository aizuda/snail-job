package com.aizuda.snail.job.server.job.task.support.block.workflow;

import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import com.aizuda.snail.job.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.snail.job.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.aizuda.snail.job.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.aizuda.snail.job.server.job.task.enums.BlockStrategyEnum;
import com.aizuda.snail.job.server.job.task.support.handler.WorkflowBatchHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: shuguang.zhang
 * @date : 2023-12-26
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class ConcurrencyWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {
    private final WorkflowBatchGenerator workflowBatchGenerator;
    private final WorkflowBatchHandler workflowBatchHandler;

    @Override
    protected void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext) {

        try {
            workflowBatchHandler.checkWorkflowExecutor(workflowBlockStrategyContext.getWorkflowTaskBatchId(), null);
        } catch (IOException e) {
            throw new EasyRetryServerException("校验工作流失败", e);
        }

        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(workflowBlockStrategyContext);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.CONCURRENCY;
    }
}
