package com.aizuda.easy.retry.client.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 服务端调度重试入参
 *
 * @auther www.byteblogs.com
 * @date 2022/03/25 10:06
 */
@Data
public class DispatchRetryDTO {
    @NotBlank(message = "scene 不能为空")
    private String scene;
    @NotBlank(message = "参数 不能为空")
    private String argsStr;
    @NotBlank(message = "idempotentId 不能为空")
    private String idempotentId;
    @NotBlank(message = "executorName 不能为空")
    private String executorName;
    @NotBlank(message = "uniqueId 不能为空")
    private String uniqueId;
    @NotNull(message = "retryCount 不能为空")
    private Integer retryCount;
}
