package com.aizuda.snailjob.model.request;

import com.aizuda.snailjob.model.request.base.WorkflowTriggerRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowTriggerApiRequest extends WorkflowTriggerRequest {

    @NotNull(message = "jobId cannot be null")
    @Deprecated
    private Long jobId;

    /**
     * 临时任务参数
     */
    @Deprecated
    private String tmpArgsStr;
}
