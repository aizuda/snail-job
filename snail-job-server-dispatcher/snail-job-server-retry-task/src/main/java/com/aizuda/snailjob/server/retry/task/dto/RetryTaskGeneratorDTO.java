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
public class RetryTaskGeneratorDTO extends BaseDTO {

    private Integer taskStatus;

    private Integer operationReason;

    private Integer taskType;

    private long nextTriggerAt;

    private Integer retryTaskExecutorScene;
}
