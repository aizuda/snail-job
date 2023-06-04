package com.aizuda.easy.retry.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author www.byteblogs.com
 * @date 2023-06-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum TaskTypeEnum {
    RETRY(1),
    CALLBACK(2);

    private final Integer type;
}
