package com.aizuda.easy.retry.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * id生成模式
 *
 * @author www.byteblogs.com
 * @date 2023-05-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum IdGeneratorMode {

    SNOWFLAKE(1, "雪花算法模式"),
    SEGMENT(2,"号段模式");

    private final int mode;

    private final String desc;

}
