package com.aizuda.snailjob.server.common.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestUpdateRetryStatusVO {
    @NotNull(message = "id 不能为空")
    private Long id;

    @NotNull(message = "retryStatus 不能为空")
    private Integer retryStatus;
}
