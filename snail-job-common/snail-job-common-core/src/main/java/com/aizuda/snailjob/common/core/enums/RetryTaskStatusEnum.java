package com.aizuda.snailjob.common.core.enums;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 重试任务状态终态枚举
 *
 * @author: opensnail
 * @date : 2021-11-03 11:05
 */
@Getter
@AllArgsConstructor
public enum RetryTaskStatusEnum {

    /**
     * 待处理
     */
    WAITING(1),

    /**
     * 重试中
     */
    RUNNING(2),

    /**
     * 重试完成
     */
    SUCCESS(3),

    /**
     * 重试失败
     */
    FAIL(4),

    /**
     * 任务停止
     */
    STOP(5),

    /**
     * 取消
     */
    CANCEL(6),
    ;

    private final Integer status;


    public static final List<Integer> NOT_COMPLETE = Arrays.asList(WAITING.status, RUNNING.status);

    public static final Set<Integer> TERMINAL_STATUS_SET = Sets.newHashSet(SUCCESS.getStatus(), FAIL.getStatus(),
            STOP.getStatus(), CANCEL.getStatus());

    public static RetryTaskStatusEnum getByStatus(@NonNull Integer status) {
        for (RetryTaskStatusEnum value : RetryTaskStatusEnum.values()) {
            if (Objects.equals(value.status, status)) {
                return value;
            }
        }
        return null;
    }

}
