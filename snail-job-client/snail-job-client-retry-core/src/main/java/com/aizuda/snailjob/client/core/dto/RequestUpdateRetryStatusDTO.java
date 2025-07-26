package com.aizuda.snailjob.client.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestUpdateRetryStatusDTO {
    @NotNull(message = "id cannot be null")
    private Long id;

    @NotNull(message = "retryStatus cannot be null")
    @Deprecated
    private Integer retryStatus;

    @NotNull(message = "retryStatus cannot be null")
    private Integer status;
}
