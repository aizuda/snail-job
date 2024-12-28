package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class WorkflowTriggerVO {

    @NotNull(message = "workflowId 不能为空")
    private Long workflowId;

    /**
     * 临时工作流上下文
     */
    private String tmpWfContext;
}
