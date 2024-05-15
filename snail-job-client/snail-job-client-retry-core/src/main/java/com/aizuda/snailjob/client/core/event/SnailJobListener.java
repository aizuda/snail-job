package com.aizuda.snailjob.client.core.event;

/**
 * @author: opensnail
 * @date : 2022-03-03 15:50
 */
public interface SnailJobListener {
    void beforeRetry(String sceneName, String executorClassName, Object[] params);

    void successOnRetry(Object result, String sceneName, String executorClassName);

    void failureOnRetry(String sceneName, String executorClassName, Throwable e);
}
