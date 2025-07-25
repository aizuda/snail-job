package com.aizuda.snailjob.server.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-25
 */
@Data
public class TriggerRetryDTO {
    @NotNull
    private Long id;
}
