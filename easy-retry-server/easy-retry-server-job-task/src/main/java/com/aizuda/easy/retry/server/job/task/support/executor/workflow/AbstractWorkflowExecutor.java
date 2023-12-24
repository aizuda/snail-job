package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import com.aizuda.easy.retry.server.job.task.support.WorkflowExecutor;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:15:19
 * @since 2.6.0
 */
public abstract class AbstractWorkflowExecutor implements WorkflowExecutor, InitializingBean {

    @Override
    public void execute(WorkflowExecutorContext context) {

        doExecute(context);
    }

    protected abstract void doExecute(WorkflowExecutorContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        WorkflowExecutorFactory.registerJobExecutor(getWorkflowNodeType(), this);
    }
}
