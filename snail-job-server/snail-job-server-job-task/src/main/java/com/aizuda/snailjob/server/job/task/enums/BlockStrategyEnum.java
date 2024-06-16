package com.aizuda.snailjob.server.job.task.enums;

import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@AllArgsConstructor
@Getter
public enum BlockStrategyEnum {
    DISCARD(1),
    OVERLAY(2),
    CONCURRENCY(3),
    /**
     * 丢弃新的并重新触发老的任务(失败的任务)
     */
    RECOVERY(4);
    ;

    private final int blockStrategy;

    public static BlockStrategyEnum valueOf(int blockStrategy) {
        for (final BlockStrategyEnum value : BlockStrategyEnum.values()) {
            if (value.blockStrategy == blockStrategy) {
                return value;
            }
        }

        throw new SnailJobServerException("不符合的阻塞策略. blockStrategy:[{}]", blockStrategy);
    }

}
