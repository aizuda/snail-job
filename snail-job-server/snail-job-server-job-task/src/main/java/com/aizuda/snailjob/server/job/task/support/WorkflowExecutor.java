package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.common.core.enums.WorkflowNodeTypeEnum;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.workflow.WorkflowExecutorContext;

/**
 * @author opensnail
 * @date 2023-09-24 11:40:21
 * @since 2.4.0
 */
public interface WorkflowExecutor {

    WorkflowNodeTypeEnum getWorkflowNodeType();

    void execute(WorkflowExecutorContext context);
}
