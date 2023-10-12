package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.server.job.task.support.strategy.BlockStrategies.BlockStrategyContext;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:53
 */
public interface BlockStrategy {
    void block(BlockStrategyContext context);
}
