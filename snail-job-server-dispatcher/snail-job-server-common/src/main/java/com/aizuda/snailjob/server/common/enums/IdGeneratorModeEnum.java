package com.aizuda.snailjob.server.common.enums;

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

    SEGMENT(1, "Number segment mode"),
    SNOWFLAKE(2, "Snowflake algorithm mode");

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
