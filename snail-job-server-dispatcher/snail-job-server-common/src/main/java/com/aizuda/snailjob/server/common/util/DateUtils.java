package com.aizuda.snailjob.server.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYYMMDDHHMMSS;
import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * @author opensnail
 * @date 2023-11-02 23:42:53
 * @since 2.4.0
 */
public class DateUtils {

    public static final DateTimeFormatter NORM_DATETIME_PATTERN = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    public static final DateTimeFormatter PURE_DATETIME_MS_PATTERN = DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS);

    private static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();;

    private DateUtils() {
    }

    public static long toEpochMilli(Date date) {
        return toLocalDateTime(date.getTime()).atZone(SYSTEM_ZONE_ID).toInstant().toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, NORM_DATETIME_PATTERN);
    }

    public static long toEpochMilli(LocalDateTime date) {
        return date.atZone(SYSTEM_ZONE_ID).toInstant().toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(Long milli) {
        if (milli == null || milli == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), SYSTEM_ZONE_ID);
    }

    public static long toNowMilli() {
        return System.currentTimeMillis();
    }

    public static LocalDateTime toNowLocalDateTime() {
        return LocalDateTime.now();
    }

    public static String format(LocalDateTime time) {
        return format(time, NORM_DATETIME_PATTERN);
    }

    public static String format(LocalDateTime time, DateTimeFormatter dateFormatter) {
        return time.format(dateFormatter);
    }

    public static String toNowFormat(DateTimeFormatter dateFormatter) {
        return format(toNowLocalDateTime(), dateFormatter);
    }

    public static long toEpochMilli(long second) {
        return second * 1000L;
    }

}
