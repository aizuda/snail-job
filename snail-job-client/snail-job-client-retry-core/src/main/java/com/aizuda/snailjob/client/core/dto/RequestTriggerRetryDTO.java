package com.aizuda.snailjob.client.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestTriggerRetryDTO {
    @NotNull(message = "id 不能为空")
    private Long id;
}
