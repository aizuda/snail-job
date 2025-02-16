package com.aizuda.snailjob.client.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 服务端调度重试入参
 *
 * @auther opensnail
 * @date 2022/03/25 10:06
 */
@Data
public class RetryCallbackDTO {
    @NotBlank(message = "group 不能为空")
    private String group;
    @NotBlank(message = "scene 不能为空")
    private String scene;
    @NotBlank(message = "参数 不能为空")
    private String argsStr;
    @NotBlank(message = "idempotentId 不能为空")
    private String idempotentId;
    @NotBlank(message = "executorName 不能为空")
    private String executorName;
    @NotNull(message = "retryStatus 不能为空")
    private Integer retryStatus;
    @NotNull(message = "retryTaskId 不能为空")
    private Long retryTaskId;
    @NotNull(message = "retryId 不能为空")
    private Long retryId;
    @NotBlank(message = "namespaceId 不能为空")
    private String namespaceId;
}
