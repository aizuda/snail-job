package com.aizuda.snail.job.client.core.callback;

/**
 * @author: opensnail
 * @date : 2023-01-10 14:46
 */
public interface RetryCompleteCallback {

    void doSuccessCallback(String sceneName, String executorName, Object[] params);

    void doMaxRetryCallback(String sceneName, String executorName, Object[] params);
}
