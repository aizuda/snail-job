package com.aizuda.snailjob.server.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-10-15 16:06:20
 * @since 2.4.0
 */
@Data
public class JobStatusUpdateRequestBaseDTO {

    @NotNull(message = "id cannot be null")
    private Long id;

    @NotNull(message = "jobStatus cannot be null")
    private Integer jobStatus;

}
