package com.aizuda.easy.retry.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 年、月、日
 *
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/03/26
 */
@AllArgsConstructor
@Getter
public enum DashboardLineEnum {

    DAY("DAY", "%H"),
    WEEK("WEEK", "%Y-%m-%d"),
    MONTH("MONTH", "%Y-%m-%d"),
    YEAR("YEAR", "%Y-%m"),
    ;

    private final String unit;
    private final String dateFormat;

    public static DashboardLineEnum modeOf(String mode) {
        for (DashboardLineEnum value : DashboardLineEnum.values()) {
            if (value.getUnit().equals(mode)) {
                return value;
            }
        }

        return DashboardLineEnum.WEEK;
    }
}
