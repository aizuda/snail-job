package com.aizuda.snailjob.model.request.base;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class JobTriggerRequest {

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;
}
