package com.aizuda.snailjob.server.job.task.support.block.workflow;

import com.aizuda.snailjob.server.job.task.enums.BlockStrategyEnum;
import com.aizuda.snailjob.server.job.task.support.BlockStrategy;

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

    static void registerBlockStrategy(BlockStrategyEnum blockStrategyEnum, BlockStrategy blockStrategy) {
        CACHE.put(blockStrategyEnum, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(Integer blockStrategy) {
        return CACHE.get(BlockStrategyEnum.valueOf(blockStrategy));
    }

}
