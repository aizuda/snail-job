package com.aizuda.snailjob.server.retry.task.support.result;

import com.aizuda.snailjob.common.core.enums.RetryResultStatusEnum;
import com.aizuda.snailjob.server.retry.task.dto.BaseDTO;
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
public class RetryResultContext extends BaseDTO {

    private RetryResultStatusEnum resultStatus;

    private boolean incrementRetryCount;

    private String resultJson;
    private String idempotentId;
    private String exceptionMsg;

}
