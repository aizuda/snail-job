package com.aizuda.snail.job.client.core.retryer;

import com.aizuda.snail.job.common.core.enums.RetryResultStatusEnum;
import lombok.Data;

/**
 * @author: opensnail
 * @date : 2022-03-04 14:52
 */
@Data
public class RetryerResultContext {

    private Object result;

    private RetryerInfo retryerInfo;

    private RetryResultStatusEnum retryResultStatusEnum;

    private String message;

    private Throwable throwable;

}