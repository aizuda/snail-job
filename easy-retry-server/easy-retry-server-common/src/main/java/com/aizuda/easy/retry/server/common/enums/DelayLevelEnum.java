package com.aizuda.easy.retry.server.common.enums;

import lombok.Getter;

import java.time.temporal.ChronoUnit;

/**
 * 延迟等级
 *
 * @author: opensnail
 * @date : 2021-11-29 17:30
 */
@Getter
public enum DelayLevelEnum {

    _1(1,10, ChronoUnit.SECONDS),
    _2(2,15, ChronoUnit.SECONDS),
    _3(3,30, ChronoUnit.SECONDS),
    _4(4,35, ChronoUnit.SECONDS),
    _5(5,40, ChronoUnit.SECONDS),
    _6(6,50, ChronoUnit.SECONDS),
    _7(7,1, ChronoUnit.MINUTES),
    _8(8,2, ChronoUnit.MINUTES),
    _9(9,4, ChronoUnit.MINUTES),
    _10(10,6, ChronoUnit.MINUTES),
    _11(11, 8, ChronoUnit.MINUTES),
    _12(12, 10, ChronoUnit.MINUTES),
    _13(13, 20, ChronoUnit.MINUTES),
    _14(14, 40, ChronoUnit.MINUTES),
    _15(15, 1, ChronoUnit.HOURS),
    _16(16, 2, ChronoUnit.HOURS),
    _17(17, 3, ChronoUnit.HOURS),
    _18(18, 4, ChronoUnit.HOURS),
    _19(19, 5, ChronoUnit.HOURS),
    _20(20, 6, ChronoUnit.HOURS),
    _21(21, 7, ChronoUnit.HOURS),
    _22(22, 8, ChronoUnit.HOURS),
    _23(23, 9, ChronoUnit.HOURS),
    _24(24, 10, ChronoUnit.HOURS),
    _25(25, 11, ChronoUnit.HOURS),
    _26(26, 12, ChronoUnit.HOURS),
    ;

    /**
     * 时间
     */
    private final int time;

    /**
     * 等级
     */
    private final int level;

    /**
     * 单位
     */
    private final ChronoUnit unit;

    DelayLevelEnum(int level, int time, ChronoUnit unit) {
        this.time = time;
        this.unit = unit;
        this.level = level;
    }

    /**
     * 根据等级获取延迟等级枚举
     *
     * @param level 等级
     * @return 延迟等级枚举
     */
    public static DelayLevelEnum getDelayLevelByLevel(int level) {

        for (DelayLevelEnum value : DelayLevelEnum.values()) {
            if (value.level == level) {
                return value;
            }
        }

        // 若配置的不存在默认1个小时一次
       return DelayLevelEnum._15;
    }

}
