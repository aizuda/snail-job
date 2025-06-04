package com.aizuda.snailjob.client.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetryDeserializeRequest {
    @NotBlank(message = "group cannot be null")
    private String group;
    @NotBlank(message = "scene cannot be null")
    private String scene;
    @NotBlank(message = "executorName cannot be null")
    private String executorName;
    @NotBlank(message = "parameters cannot be null")
    private String argsStr;
    @NotBlank(message = "retryArgSerializerName cannot be null")
    private String retryArgSerializerName;
}
