package com.aizuda.snailjob.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 基于 bizId 的任务触发请求
 *
 * @author opensnail
 * @since 1.10.0
 */
@Data
public class JobTriggerBizIdRequest {

    /**
     * 业务ID
     */
    @NotBlank(message = "bizId cannot be blank")
    private String bizId;

    /**
     * 临时任务参数
     */
    private String tmpArgsStr;
}