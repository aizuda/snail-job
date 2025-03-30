package com.aizuda.snailjob.server.common.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestTriggerRetryVO {
    @NotNull(message = "id 不能为空")
    private Long id;
}
