package com.aizuda.snailjob.model.base;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class WorkflowTriggerRequest {

    @NotNull(message = "workflowId cannot be null")
    private Long workflowId;

    /**
     * 上下文
     */
    private String tmpWfContext;
}
