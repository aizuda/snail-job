package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: xiaowoniu
 * @date : 2024-01-09
 * @since : 2.6.0
 */
@AllArgsConstructor
@Getter
public enum JobArgsTypeEnum {
    TEXT(1, "文本"),
    JSON(2, "JSON");

    private final Integer argsType;
    private final String desc;

}
