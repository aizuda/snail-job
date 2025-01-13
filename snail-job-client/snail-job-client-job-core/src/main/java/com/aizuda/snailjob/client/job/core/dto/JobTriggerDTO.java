package com.aizuda.snailjob.client.job.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class JobTriggerDTO {

    @NotNull(message = "jobId 不能为空")
    private Long jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;
}
