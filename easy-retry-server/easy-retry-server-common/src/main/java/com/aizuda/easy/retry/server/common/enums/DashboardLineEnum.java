package com.aizuda.easy.retry.server.common.enums;

import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.utils.DbUtils;
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
    DAY("DAY"),
    WEEK("WEEK"),
    MONTH("MONTH"),
    YEAR("YEAR"),
    ;

    private final String unit;

    public static DashboardLineEnum modeOf(String mode) {
        for (DashboardLineEnum value : DashboardLineEnum.values()) {
            if (value.getUnit().equals(mode)) {
                return value;
            }
        }

        return DashboardLineEnum.WEEK;
    }

    public static String dateFormat(String unit) {
        DashboardLineEnum mode = modeOf(unit);

        if (DbUtils.getDbType().equals(DbTypeEnum.MYSQL)) {
            switch (mode) {
                case YEAR: return "%Y-%m";
                case DAY: return "%H";
                default: return "%Y-%m-%d";
            }
        } else if (DbUtils.getDbType().equals(DbTypeEnum.MARIADB)) {
            switch (mode) {
                case YEAR: return "%Y-%m";
                case DAY: return "%H";
                default: return "%Y-%m-%d";
            }
        } else if (DbUtils.getDbType().equals(DbTypeEnum.SQLSERVER)) {
            switch (mode) {
                case YEAR: return "yyyy-MM";
                case DAY: return "HH";
                default: return "yyyy-MM-dd";
            }
        } else { // Oracle, Postgres
            switch (mode) {
                case YEAR: return "yyyy-MM";
                case DAY: return "HH24";
                default: return "yyyy-MM-dd";
            }
        }
    }

}
