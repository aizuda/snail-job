package com.aizuda.easy.retry.server.job.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-26 14:26
 */
@AllArgsConstructor
@Getter
public enum TaskStatusEnum {

    /**
     * 待处理
     */
    WAIT(10),

    /**
     * 处理中
     */
    PROCESSING(20),

    /**
     * 处理中
     */
    PROCESSED_SUCCESS(21),

    /**
     * 处理中
     */
    PROCESSED_FAIL(22),

    /**
     * 中断中
     */
    INTERRUPTING(30),

    /**
     * 中断成功
     */
    INTERRUPT_SUCCESS(31),

    /**
     * 中断失败
     */
    INTERRUPT_FAIL(32),

    ;

    private final int status;
}
