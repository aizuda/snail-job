package com.aizuda.snail.job.server.common.util;

import com.aizuda.snail.job.common.core.util.CronExpression;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author opensnail
 * @date 2023-11-02 22:52:10
 * @since 2.4.0
 */
public class CronUtils {

    public static List<String> getExecuteTimeByCron(String cron, int nums) {

        List<String> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < nums; i++) {
            Date nextValidTime;
            try {
                ZonedDateTime zdt = now.atZone(ZoneOffset.ofHours(8));
                nextValidTime = new CronExpression(cron).getNextValidTimeAfter(Date.from(zdt.toInstant()));
                now = LocalDateTime.ofEpochSecond(nextValidTime.getTime() / 1000, 0, ZoneOffset.ofHours(8));
                list.add(DateUtils.format(now, DateUtils.NORM_DATETIME_PATTERN));
            } catch (ParseException ignored) {
            }
        }

        return list;
    }

    public static long getExecuteInterval(String cron) {
        List<String> executeTimeByCron = getExecuteTimeByCron(cron, 2);
        LocalDateTime first = LocalDateTime.parse(executeTimeByCron.get(0), DateUtils.NORM_DATETIME_PATTERN);
        LocalDateTime second = LocalDateTime.parse(executeTimeByCron.get(1), DateUtils.NORM_DATETIME_PATTERN);
        Duration duration = Duration.between(first, second);
        return duration.toMillis();
    }

}
