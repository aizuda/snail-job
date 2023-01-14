package com.x.retry.server.support;

/**
 * 重试停止策略
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:43
 */
public interface StopStrategy {

    /**
     * 重试停止
     *
     * @param retryContext {@link RetryContext} 重试上下文
     * @return
     */
    boolean shouldStop(RetryContext retryContext);
}
