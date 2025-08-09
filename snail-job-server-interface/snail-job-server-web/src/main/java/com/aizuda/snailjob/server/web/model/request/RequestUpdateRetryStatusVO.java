package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Deprecated
public class RequestUpdateRetryStatusVO {
    @NotNull(message = "id cannot be null")
    private Long id;

    @NotNull(message = "retryStatus cannot be null")
    private Integer retryStatus;
}
