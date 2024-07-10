package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 重试结果状态
 *
 * @author: opensnail
 * @date : 2021-11-03 11:05
 */
@AllArgsConstructor
@Getter
public enum RetryResultStatusEnum {

    SUCCESS(0),
    STOP(1),
    FAILURE(2);

    private final Integer status;

    public static RetryResultStatusEnum getRetryResultStatusEnum(int status) {
        for (RetryResultStatusEnum value : RetryResultStatusEnum.values()) {
            if (value.status == status) {
                return value;
            }
        }

        return null;
    }
}
