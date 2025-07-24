package com.aizuda.snailjob.server.common.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Deprecated
public class RequestTriggerRetryVO {
    @NotNull(message = "id cannot be null")
    private Long id;
}
