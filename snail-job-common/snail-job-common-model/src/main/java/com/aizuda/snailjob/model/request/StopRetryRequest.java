package com.aizuda.snailjob.model.request;

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
public class StopRetryRequest {
    @NotBlank(message = "namespaceId cannot be null")
    private String namespaceId;
    @NotBlank(message = "group cannot be null")
    private String groupName;
    @NotBlank(message = "scene cannot be null")
    private String scene;
    @NotNull(message = "retryTaskId cannot be null")
    private Long retryTaskId;
    @NotNull(message = "retryId cannot be null")
    private Long retryId;
}
