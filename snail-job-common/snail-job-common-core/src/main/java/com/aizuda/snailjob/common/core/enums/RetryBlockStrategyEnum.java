package com.aizuda.snailjob.common.core.enums;

import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 阻塞策略针对处于待处理 or 运行中的重试任务做了一种异常容错策略
 *
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@AllArgsConstructor
@Getter
public enum RetryBlockStrategyEnum {

    /**
     * 不创建新的重试任务，等待当前重试任务执行完成
     */
    DISCARD(1),
    /**
     * 停止当前的重试任务，然后新增一个新的重试任务
     */
    OVERLAY(2),
    /**
     * 每次都创建一个新的重试任务
     */
    CONCURRENCY(3),
    ;

    private final int blockStrategy;

    public static RetryBlockStrategyEnum valueOf(int blockStrategy) {
        for (final RetryBlockStrategyEnum value : RetryBlockStrategyEnum.values()) {
            if (value.blockStrategy == blockStrategy) {
                return value;
            }
        }

        throw new SnailJobCommonException("不符合的阻塞策略. blockStrategy:[{}]", blockStrategy);
    }

}
