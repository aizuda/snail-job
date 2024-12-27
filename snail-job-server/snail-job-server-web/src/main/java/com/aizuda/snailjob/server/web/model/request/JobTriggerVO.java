package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class JobTriggerVO {

    @NotBlank(message = "jobId 不能为空")
    private Long jobId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;
}
