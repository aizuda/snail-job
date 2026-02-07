package com.aizuda.snailjob.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 基于 bizId 的任务状态更新请求
 *
 * @author opensnail
 * @since 1.10.0
 */
@Data
public class StatusUpdateBizIdRequest {

    /**
     * 业务ID
     */
    @NotBlank(message = "bizId cannot be blank")
    private String bizId;

    @NotNull(message = "status cannot be null")
    private Integer status;
}