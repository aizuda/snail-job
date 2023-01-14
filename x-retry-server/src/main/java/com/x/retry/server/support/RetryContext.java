package com.x.retry.server.support;

import com.x.retry.server.persistence.mybatis.po.RetryTask;

/**
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:35
 */
public interface RetryContext<V> {

    /**
     * 获取重试数据
     *
     * @return
     */
    RetryTask getRetryTask();

    /**
     * 回调接果
     *
     * @param v 回调的数据
     */
    void setCallResult(V v);

    /**
     * 等待策略
     *
     * @param waitStrategy {@link WaitStrategy} 等待策略
     */
    void setWaitStrategy(WaitStrategy waitStrategy);
}
