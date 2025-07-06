package com.aizuda.snailjob.server.job.task.support.block.workflow;

import com.aizuda.snailjob.common.core.enums.JobBlockStrategyEnum;
import com.aizuda.snailjob.server.job.task.support.BlockStrategy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xiaowoniu
 * @date : 2023-12-26
 * @since : 2.6.0
 */
public final class WorkflowBlockStrategyFactory {
    private static final ConcurrentHashMap<JobBlockStrategyEnum, BlockStrategy> CACHE = new ConcurrentHashMap<>();

    private WorkflowBlockStrategyFactory() {
    }

    static void registerBlockStrategy(JobBlockStrategyEnum jobBlockStrategyEnum, BlockStrategy blockStrategy) {
        CACHE.put(jobBlockStrategyEnum, blockStrategy);
    }

    public static BlockStrategy getBlockStrategy(Integer blockStrategy) {
        return CACHE.get(JobBlockStrategyEnum.valueOf(blockStrategy));
    }

}
