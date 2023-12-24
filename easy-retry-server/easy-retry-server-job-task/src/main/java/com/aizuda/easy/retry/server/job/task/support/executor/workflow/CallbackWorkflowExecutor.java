package com.aizuda.easy.retry.server.job.task.support.executor.workflow;

import com.aizuda.easy.retry.common.core.enums.WorkflowNodeTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @author xiaowoniu
 * @date 2023-12-24 08:18:06
 * @since 2.6.0
 */
@Component
public class CallbackWorkflowExecutor extends AbstractWorkflowExecutor {
    @Override
    public WorkflowNodeTypeEnum getWorkflowNodeType() {
        return WorkflowNodeTypeEnum.CALLBACK;
    }

    @Override
    protected void doExecute(WorkflowExecutorContext context) {

    }
}
