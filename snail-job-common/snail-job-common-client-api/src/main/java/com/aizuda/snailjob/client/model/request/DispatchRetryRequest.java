package com.aizuda.snailjob.client.model.request;

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
    @NotBlank(message = "namespaceId 不能为空")
    private String namespaceId;
    @NotBlank(message = "groupName 不能为空")
    private String groupName;
    @NotBlank(message = "sceneName 不能为空")
    private String sceneName;
    @NotBlank(message = "参数 不能为空")
    private String argsStr;
    @NotBlank(message = "executorName 不能为空")
    private String executorName;
    @NotNull(message = "retryCount 不能为空")
    private Integer retryCount;
    @NotNull(message = "retryTaskId 不能为空")
    private Long retryTaskId;
    @NotNull(message = "retryId 不能为空")
    private Long retryId;
}
