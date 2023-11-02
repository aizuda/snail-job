package com.aizuda.easy.retry.server.common;

import com.aizuda.easy.retry.server.common.strategy.WaitStrategies.WaitStrategyContext;

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
    Long computeTriggerTime(WaitStrategyContext waitStrategyContext);

}
