package com.aizuda.snailjob.server.common.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestUpdateRetryStatusVO {
    @NotNull(message = "id cannot be null")
    private Long id;

    @NotNull(message = "retryStatus cannot be null")
    private Integer retryStatus;
}
