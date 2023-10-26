package com.aizuda.easy.retry.server.retry.task.support;

import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies.WaitStrategyContext;

import java.time.LocalDateTime;

/**
 * 等待策略（退避策略）
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:18
 */
public interface WaitStrategy {

    /**
     * 计算下次重试触发时间
     *
     * @param waitStrategyContext {@link WaitStrategyContext} 重试上下文
     * @return 下次触发时间
     */
    LocalDateTime computeRetryTime(WaitStrategyContext waitStrategyContext);

}
