package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryTaskExecuteDTO extends BaseDTO {

    private Integer routeKey;
}

