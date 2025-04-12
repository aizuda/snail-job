package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2023-12-26 22:16:51
 * @since 2.6.0
 */
@Getter
@AllArgsConstructor
public enum FailStrategyEnum {

    SKIP(1, "Skip"),
    BLOCK(2, "Block");

    private final Integer code;
    private final String desc;

    public static FailStrategyEnum valueOf(Integer code) {
        for (FailStrategyEnum failStrategyEnum : FailStrategyEnum.values()) {
            if (Objects.equals(failStrategyEnum.code, code)) {
                return failStrategyEnum;
            }
        }

        return null;
    }

}
