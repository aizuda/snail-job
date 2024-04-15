package com.aizuda.snail.job.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * id生成模式
 *
 * @author opensnail
 * @date 2023-05-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum IdGeneratorModeEnum {

    SEGMENT(1,"号段模式"),
    SNOWFLAKE(2, "雪花算法模式");

    private final int mode;

    private final String desc;

    public static IdGeneratorModeEnum modeOf(int mode) {
        for (IdGeneratorModeEnum value : IdGeneratorModeEnum.values()) {
            if (value.getMode() == mode) {
                return value;
            }
        }

        return null;
    }

}
