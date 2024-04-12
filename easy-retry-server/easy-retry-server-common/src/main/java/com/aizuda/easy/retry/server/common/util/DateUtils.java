package com.aizuda.easy.retry.server.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author opensnail
 * @date 2023-11-02 23:42:53
 * @since 2.4.0
 */
public class DateUtils {

   public static final DateTimeFormatter NORM_DATETIME_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter PURE_DATETIME_MS_PATTERN = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final ZoneOffset zoneOffset = ZoneOffset.of("+8");

    private DateUtils() {
    }

    public static long toEpochMilli(Date date) {
        return toLocalDateTime(date.getTime()).toInstant(zoneOffset).toEpochMilli();
    }

    public static long toEpochMilli(LocalDateTime date) {
        return date.toInstant(zoneOffset).toEpochMilli();
    }

    public static LocalDateTime toLocalDateTime(long milli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), zoneOffset);
    }

    public static long toNowMilli() {
        return System.currentTimeMillis();
    }

    public static LocalDateTime toNowLocalDateTime() {
        return LocalDateTime.now();
    }

    public static String format(LocalDateTime time, DateTimeFormatter dateFormatter) {
        return time.format(dateFormatter);
    }

    public static String toNowFormat( DateTimeFormatter dateFormatter) {
        return format(toNowLocalDateTime(), dateFormatter);
    }

    public static long toEpochMilli(long second) {
        return second * 1000L;
    }

}
