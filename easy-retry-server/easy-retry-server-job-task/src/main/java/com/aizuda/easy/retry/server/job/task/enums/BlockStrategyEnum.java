package com.aizuda.easy.retry.server.job.task.enums;

import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
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
    CONCURRENCY(3);

    private final int blockStrategy;

    public static BlockStrategyEnum valueOf(int blockStrategy) {
        for (final BlockStrategyEnum value : BlockStrategyEnum.values()) {
            if (value.blockStrategy == blockStrategy) {
                return value;
            }
        }

        throw new EasyRetryServerException("不符合的阻塞策略. blockStrategy:[{}]", blockStrategy);
    }

}
