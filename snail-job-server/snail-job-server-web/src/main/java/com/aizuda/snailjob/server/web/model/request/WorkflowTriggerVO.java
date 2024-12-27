package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class WorkflowTriggerVO {

    @NotBlank(message = "workflowId 不能为空")
    private Long workflowId;

    /**
     * 临时工作流上下文
     */
    private String tmpWfContext;
}
