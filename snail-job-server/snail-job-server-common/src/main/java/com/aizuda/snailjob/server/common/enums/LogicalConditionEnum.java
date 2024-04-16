package com.aizuda.snailjob.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaowoniu
 * @date 2024-01-01 22:56:28
 * @since 2.6.0
 */
@Getter
@AllArgsConstructor
public enum LogicalConditionEnum {
    /**
     * 逻辑条件
     */
    AND(1, "并"),
    OR(2, "或");

    private final Integer code;
    private final String desc;
}
