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
public class StopRetryRequest {
    @NotBlank(message = "namespaceId 不能为空")
    private String namespaceId;
    @NotBlank(message = "group 不能为空")
    private String groupName;
    @NotBlank(message = "scene 不能为空")
    private String scene;
    @NotNull(message = "retryTaskId 不能为空")
    private Long retryTaskId;
    @NotNull(message = "retryId 不能为空")
    private Long retryId;
}
