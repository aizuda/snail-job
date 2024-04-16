package com.aizuda.snailjob.common.core.enums;

import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * 重试状态终态枚举
 *
 * @author: opensnail
 * @date : 2021-11-03 11:05
 */
@Getter
public enum RetryStatusEnum {

    /**
     * 重试中
     */
    RUNNING(0),

    /**
     * 重试完成
     */
    FINISH(1),

    /**
     * 到达最大次数
     */
    MAX_COUNT(2),

    /**
     * 暂停重试
     */
    SUSPEND(3);

    private final Integer status;

    RetryStatusEnum(int status) {
        this.status = status;
    }

    public static RetryStatusEnum getByStatus(@NonNull Integer status) {

        for (RetryStatusEnum value : RetryStatusEnum.values()) {
            if (Objects.equals(value.status, status)) {
                return value;
            }
        }

        return null;
    }

}
