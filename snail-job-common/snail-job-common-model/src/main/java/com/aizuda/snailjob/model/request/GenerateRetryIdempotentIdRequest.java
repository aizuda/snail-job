package com.aizuda.snailjob.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成idempotentId模型
 *
 * @auther opensnail
 * @date 2022/03/25 10:06
 */
@Data
public class GenerateRetryIdempotentIdRequest {
    @NotBlank(message = "group cannot be null")
    private String group;
    @NotBlank(message = "scene cannot be null")
    private String scene;
    @NotBlank(message = "parameters cannot be null")
    private String argsStr;
    @NotBlank(message = "executorName cannot be null")
    private String executorName;
    @NotBlank(message = "serializerName cannot be null")
    private String serializerName;
}
