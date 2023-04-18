package com.aizuda.easy.retry.client.core.strategy;

import com.aizuda.easy.retry.client.core.retryer.RetryType;
import com.aizuda.easy.retry.client.core.retryer.RetryerResultContext;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 14:33
 */
public interface RetryStrategy {

    boolean supports(int stage, RetryType retryType);

    RetryerResultContext openRetry(String sceneName, String executorClassName, Object[] params);

}
