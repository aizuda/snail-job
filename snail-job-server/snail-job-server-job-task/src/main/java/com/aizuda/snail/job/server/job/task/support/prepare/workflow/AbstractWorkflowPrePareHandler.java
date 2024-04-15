package com.aizuda.snail.job.server.job.task.support.prepare.workflow;

import com.aizuda.snail.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snail.job.server.job.task.support.WorkflowPrePareHandler;

/**
 * @author: xiaowoniu
 * @date : 2023-12-22 08:57
 * @since : 2.6.0
 */
public abstract class AbstractWorkflowPrePareHandler implements WorkflowPrePareHandler {

    @Override
    public void handler(WorkflowTaskPrepareDTO workflowTaskPrepareDTO) {

        doHandler(workflowTaskPrepareDTO);
    }

    protected abstract void doHandler(WorkflowTaskPrepareDTO jobPrepareDTO);

}
