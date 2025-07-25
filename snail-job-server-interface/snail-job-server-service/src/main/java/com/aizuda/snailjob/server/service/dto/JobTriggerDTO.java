package com.aizuda.snailjob.server.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class JobTriggerDTO {

    @NotNull(message = "jobId cannot be null")
    private Long jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;
}
