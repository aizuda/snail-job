package com.aizuda.snailjob.server.job.task.support.block.workflow;

import com.aizuda.snailjob.common.core.enums.BlockStrategyEnum;
import com.aizuda.snailjob.server.job.task.support.BlockStrategy;
import com.aizuda.snailjob.server.job.task.support.block.job.BlockStrategyContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: xiaowoniu
 * @date : 2023-12-26
 * @since : 2.6.0
 */
public abstract class AbstractWorkflowBlockStrategy implements BlockStrategy, InitializingBean {

    @Override
    @Transactional
    public void block(final BlockStrategyContext context) {
        WorkflowBlockStrategyContext workflowBlockStrategyContext = (WorkflowBlockStrategyContext) context;

        doBlock(workflowBlockStrategyContext);
    }

    protected abstract void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext);

    protected abstract BlockStrategyEnum blockStrategyEnum();

    @Override
    public void afterPropertiesSet() throws Exception {
        WorkflowBlockStrategyFactory.registerBlockStrategy(blockStrategyEnum(), this);
    }
}
