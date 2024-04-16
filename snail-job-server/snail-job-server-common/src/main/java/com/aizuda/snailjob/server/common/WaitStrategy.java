package com.aizuda.snailjob.server.common;

import com.aizuda.snailjob.server.common.strategy.WaitStrategies;

/**
 * 等待策略（退避策略）
 *
 * @author: opensnail
 * @date : 2021-11-29 18:18
 */
public interface WaitStrategy {

    /**
     * 计算下次重试触发时间
     *
     * @param waitStrategyContext {@link WaitStrategies.WaitStrategyContext} 重试上下文
     * @return 下次触发时间
     */
    Long computeTriggerTime(WaitStrategies.WaitStrategyContext waitStrategyContext);

}
