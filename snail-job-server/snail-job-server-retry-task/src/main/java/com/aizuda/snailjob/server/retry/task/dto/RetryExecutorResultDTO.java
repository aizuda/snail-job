package com.aizuda.snailjob.server.retry.task.dto;

import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
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

    private Integer resultStatus;
    private Integer operationReason;
    private boolean incrementRetryCount;
    private String resultJson;
    private Integer statusCode;
    private String idempotentId;
    private String exceptionMsg;

}
