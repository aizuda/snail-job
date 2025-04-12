package com.aizuda.snailjob.client.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestTriggerRetryDTO {
    @NotNull(message = "id cannot be null")
    private Long id;
}
