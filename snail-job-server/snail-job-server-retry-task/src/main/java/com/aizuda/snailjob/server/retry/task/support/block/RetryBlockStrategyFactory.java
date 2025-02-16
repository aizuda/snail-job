package com.aizuda.snailjob.server.retry.task.support.block;

import com.aizuda.snailjob.common.core.enums.JobBlockStrategyEnum;
import com.aizuda.snailjob.server.retry.task.support.BlockStrategy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: opensnail
 * @date : 2025-02-10
 * @since : sj_1.4.0
 */
public final class RetryBlockStrategyFactory {
    private static final ConcurrentHashMap<JobBlockStrategyEnum, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private RetryBlockStrategyFactory() {
    }

    static void registerBlockStrategy(JobBlockStrategyEnum jobBlockStrategyEnum, BlockStrategy blockStrategy) {
        CACHE.put(jobBlockStrategyEnum, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(Integer blockStrategy) {
        return CACHE.get(JobBlockStrategyEnum.valueOf(blockStrategy));
    }

}
