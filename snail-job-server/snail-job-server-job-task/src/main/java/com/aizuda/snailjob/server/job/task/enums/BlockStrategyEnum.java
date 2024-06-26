package com.aizuda.snailjob.server.job.task.enums;

import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 阻塞策略针对处于待处理 or 运行中的批次做了一种异常容错策略
 *
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@AllArgsConstructor
@Getter
public enum BlockStrategyEnum {

    /**
     * 不创建新的批次，等待当前批次执行完成
     */
    DISCARD(1),
    /**
     * 停止当前的批次，然后新增一个新的批次
     */
    OVERLAY(2),
    /**
     * 每次都创建一个新的批次
     */
    CONCURRENCY(3),
    /**
     * 不创建新的批次, 重新执行当前的批次中已经失败的任务
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
