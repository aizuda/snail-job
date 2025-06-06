package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 执行器类型枚举
 *
 * @author dhb52
 * @date 2024-07-09 23:48:55
 * @since 1.1.0
 */
@AllArgsConstructor
@Getter
public enum ExecutorTypeEnum {

    JAVA(1),
    PYTHON(2),
    GO(3),
    ;

    private final int type;

}
