package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.server.job.task.support.block.job.BlockStrategyContext;

/**
 * @author: opensnail
 * @date : 2023-09-25 17:53
 * @since : 1.0.0
 */
public interface BlockStrategy {

    /**
     * 阻塞策略
     *
     * @param context 策略上下文
     */
    void block(BlockStrategyContext context);
}
