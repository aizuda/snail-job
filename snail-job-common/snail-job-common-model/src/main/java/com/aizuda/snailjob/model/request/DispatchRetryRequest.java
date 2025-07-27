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
public class DispatchRetryRequest {
    @NotBlank(message = "namespaceId cannot be null")
    private String namespaceId;
    @NotBlank(message = "groupName cannot be null")
    private String groupName;
    @NotBlank(message = "sceneName cannot be null")
    private String sceneName;
    @NotBlank(message = "parameters cannot be null")
    private String argsStr;
    @NotBlank(message = "executorName cannot be null")
    private String executorName;
    @NotNull(message = "retryCount cannot be null")
    private Integer retryCount;
    @NotNull(message = "retryTaskId cannot be null")
    private Long retryTaskId;
    @NotNull(message = "retryId cannot be null")
    private Long retryId;
    @NotNull(message = "executorTimeout cannot be null")
    private Integer executorTimeout;
    @NotBlank(message = "serializerName cannot be null")
    private String serializerName;
}
