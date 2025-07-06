package com.aizuda.snailjob.server.retry.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryExecutorResultDTO extends BaseDTO  {

    private Integer operationReason;
    private boolean incrementRetryCount;
    private String resultJson;
    private String exceptionMsg;
    private Integer taskStatus;

}
