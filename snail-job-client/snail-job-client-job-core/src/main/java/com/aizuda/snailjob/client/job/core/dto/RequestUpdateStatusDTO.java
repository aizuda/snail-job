package com.aizuda.snailjob.client.job.core.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-15 16:06:20
 * @since 2.4.0
 */
@Data
public class RequestUpdateStatusDTO {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotNull(message = "jobStatus 不能为空")
    private Integer jobStatus;

}
