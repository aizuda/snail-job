package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.model.request.base.WorkflowTriggerRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowTriggerVO extends WorkflowTriggerRequest {

    @NotNull(message = "workflowId cannot be null")
    private Long workflowId;

    /**
     * 临时工作流上下文
     */
    private String tmpWfContext;
}
