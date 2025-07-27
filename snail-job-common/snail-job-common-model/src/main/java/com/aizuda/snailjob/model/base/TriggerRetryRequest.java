package com.aizuda.snailjob.model.base;

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
public class TriggerRetryRequest {
    @NotNull
    private Long id;
}
