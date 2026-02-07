package com.aizuda.snailjob.model.request;

import com.aizuda.snailjob.model.request.base.WorkflowTriggerRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基于 bizId 的工作流触发请求
 *
 * @author opensnail
 * @since 1.10.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowTriggerBizIdRequest extends WorkflowTriggerRequest {

    /**
     * 业务ID
     */
    @NotBlank(message = "bizId cannot be blank")
    private String bizId;

    /**
     * 上下文
     */
    private String tmpWfContext;
}