package com.aizuda.easy.retry.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-26 14:26
 */
@AllArgsConstructor
@Getter
public enum JobTaskStatusEnum {

    /**
     * 待处理
     */
    WAITING(1),

    /**
     * 处理中
     */
    RUNNING(2),

    /**
     * 处理成功
     */
    SUCCESS(3),

    /**
     * 处理失败
     */
    FAIL(4),

    /**
     * 任务停止成功
     */
    STOP(5),


    ;

    private final int status;

    public static final List<Integer> NOT_COMPLETE = Arrays.asList(WAITING.status, RUNNING.status);

    public static final List<Integer> COMPLETED = Arrays.asList(SUCCESS.status, FAIL.status, STOP.status);
}
