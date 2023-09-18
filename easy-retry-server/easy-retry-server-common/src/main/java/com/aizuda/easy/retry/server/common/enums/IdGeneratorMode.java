package com.aizuda.easy.retry.server.common.enums;

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

    SEGMENT(1,"号段模式"),
    SNOWFLAKE(2, "雪花算法模式");

    private final int mode;

    private final String desc;

    public static IdGeneratorMode modeOf(int mode) {
        for (IdGeneratorMode value : IdGeneratorMode.values()) {
            if (value.getMode() == mode) {
                return value;
            }
        }

        return null;
    }

}
