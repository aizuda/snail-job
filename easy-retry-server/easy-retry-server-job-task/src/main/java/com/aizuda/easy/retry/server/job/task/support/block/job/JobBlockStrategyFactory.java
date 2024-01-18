package com.aizuda.easy.retry.server.job.task.support.block.job;

import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.enums.BlockStrategyEnum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xiaowoniu
 * @date : 2023-01-18
 * @since : 2.6.0
 */
public final class JobBlockStrategyFactory {
    private static final ConcurrentHashMap<BlockStrategyEnum, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private JobBlockStrategyFactory() {
    }

    static void registerBlockStrategy(BlockStrategyEnum blockStrategyEnum, BlockStrategy blockStrategy) {
        CACHE.put(blockStrategyEnum, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(Integer  blockStrategy) {
        return CACHE.get(BlockStrategyEnum.valueOf(blockStrategy));
    }

}
