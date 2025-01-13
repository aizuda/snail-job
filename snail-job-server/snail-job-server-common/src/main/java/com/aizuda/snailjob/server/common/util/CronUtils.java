package com.aizuda.snailjob.server.common.util;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.util.CronExpression;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
                if (Objects.isNull(nextValidTime)) {
                    continue;
                }
                now = LocalDateTime.ofEpochSecond(nextValidTime.getTime() / 1000, 0, ZoneOffset.ofHours(8));
                list.add(DateUtils.format(now, DateUtils.NORM_DATETIME_PATTERN));
            } catch (ParseException ignored) {
            }
        }

        return list;
    }

    public static long getExecuteInterval(String cron) {
        List<String> executeTimeByCron = getExecuteTimeByCron(cron, 2);
        Assert.isTrue(!executeTimeByCron.isEmpty(), () -> new SnailJobServerException("表达式解析有误.[{}]", cron));
        Assert.isTrue(executeTimeByCron.size() == 2, () -> new SnailJobServerException("表达式必须支持多次执行.[{}]", cron));
        LocalDateTime first = LocalDateTime.parse(executeTimeByCron.get(0), DateUtils.NORM_DATETIME_PATTERN);
        LocalDateTime second = LocalDateTime.parse(executeTimeByCron.get(1), DateUtils.NORM_DATETIME_PATTERN);
        Duration duration = Duration.between(first, second);
        return duration.toMillis();
    }

}
