package com.aizuda.easy.retry.server.job.task.support.block.workflow;

import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.support.block.job.BlockStrategies.BlockStrategyEnum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xiaowoniu
 * @date : 2023-12-26
 * @since : 2.6.0
 */
public final class WorkflowBlockStrategyFactory {
    private static final ConcurrentHashMap<BlockStrategyEnum, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private WorkflowBlockStrategyFactory() {
    }

    protected static void registerTaskStop(BlockStrategyEnum blockStrategyEnum, BlockStrategy blockStrategy) {
        CACHE.put(blockStrategyEnum, blockStrategy);
    }

    public static BlockStrategy getJobTaskStop(Integer  blockStrategy) {
        return CACHE.get(BlockStrategyEnum.valueOf(blockStrategy));
    }

}
