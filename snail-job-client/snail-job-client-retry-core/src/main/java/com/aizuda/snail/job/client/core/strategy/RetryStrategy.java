package com.aizuda.snail.job.client.core.strategy;

import com.aizuda.snail.job.client.core.retryer.RetryType;
import com.aizuda.snail.job.client.core.retryer.RetryerResultContext;

/**
 * @author: opensnail
 * @date : 2022-03-03 14:33
 */
public interface RetryStrategy {

    boolean supports(int stage, RetryType retryType);

    RetryerResultContext openRetry(String sceneName, String executorClassName, Object[] params);

}
