package com.aizuda.easy.retry.server.support;

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

    /**
     * 是否触发此停止策略
     *
     * @param retryContext {@link RetryContext} 重试上下文
     * @return
     */
    boolean supports(RetryContext retryContext);

    /**
     * 按照正序排列重试过滤器
     * 若相同则按照加入的顺序
     *
     * @return 排序的值
     */
    int order();

}
