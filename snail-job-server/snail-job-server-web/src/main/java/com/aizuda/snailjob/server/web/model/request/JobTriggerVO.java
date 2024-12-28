package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class JobTriggerVO {

    @NotNull(message = "jobId 不能为空")
    private Long jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;
}
