package com.aizuda.easy.retry.common.core.enums;

import lombok.Getter;

/**
 * 重试结果状态
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-03 11:05
 */
@Getter
public enum RetryResultStatusEnum {

    SUCCESS(0),
    STOP(1),
    FAILURE(2);

    private final Integer status;

    RetryResultStatusEnum(int status) {
        this.status = status;
    }

    public static RetryResultStatusEnum getRetryResultStatusEnum(int status) {
        for (RetryResultStatusEnum value : RetryResultStatusEnum.values()) {
            if (value.status == status) {
                return value;
            }
        }

        return null;
    }
}
