package com.aizuda.snailjob.client.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestUpdateRetryStatusDTO {
    @NotNull(message = "id 不能为空")
    private Long id;

    @NotNull(message = "retryStatus 不能为空")
    private Integer retryStatus;
}
