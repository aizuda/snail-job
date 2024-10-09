package com.aizuda.snailjob.server.job.task.support.block.workflow;

import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.common.core.enums.BlockStrategyEnum;
import com.aizuda.snailjob.server.job.task.support.handler.WorkflowBatchHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: opensnail
 * @date : 2024-06-26
 * @since : sj_1.1.0
 */
@Component
@RequiredArgsConstructor
public class RecoveryWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {
    private final WorkflowBatchHandler workflowBatchHandler;

    @Override
    protected void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext) {

        try {
            workflowBatchHandler.recoveryWorkflowExecutor(workflowBlockStrategyContext.getWorkflowTaskBatchId(), null);
        } catch (IOException e) {
            throw new SnailJobServerException("校验工作流失败", e);
        }
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.RECOVERY;
    }
}
