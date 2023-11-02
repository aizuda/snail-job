package com.aizuda.easy.retry.server.common.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author www.byteblogs.com
 * @date 2023-11-02 23:42:53
 * @since 2.4.0
 */
public class DateUtil extends cn.hutool.core.date.DateUtil {

    public static long toEpochMilli(Date date) {
        return DateUtil.toLocalDateTime(date).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static long toEpochMilli(LocalDateTime date) {
        return date.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static LocalDateTime toEpochMilli(long milli) {
        return LocalDateTime.ofEpochSecond(milli / 1000, 0, ZoneOffset.ofHours(8));
    }
}
