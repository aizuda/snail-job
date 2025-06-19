package com.aizuda.snailjob.client.job.core.util;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.dto.PointInTimeDTO;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * @since 1.6.0
 */
public class TriggerIntervalUtils {

    private static final ZoneOffset zoneOffset = ZoneOffset.of("+8");

    public static final DateTimeFormatter NORM_DATETIME_PATTERN = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    private static long toEpochMilli(LocalDateTime date) {
        return date.toInstant(zoneOffset).toEpochMilli();
    }

    private static List<LocalDateTime> checkTriggerInterval(Set<LocalDateTime> triggerTime) {
            LocalDateTime now = LocalDateTime.now();
            List<LocalDateTime> localDateTimes = triggerTime
                    .stream()
                    .sorted(Comparator.naturalOrder())
                    .toList();
            LocalDateTime first = localDateTimes.get(0);
            if (first.isBefore(now)) {
                throw new SnailJobClientException("The submission time is less than the current time. triggerTime:{} now:{}", first, now);
            }

            // 判断间隔是否大于10秒
            Assert.isTrue(areAllIntervalsLessThan(localDateTimes, Duration.ofSeconds(10)),
                    () -> new SnailJobClientException("There are combinations with intervals less than 10(s)"));
            return localDateTimes;
    }

    private static boolean areAllIntervalsLessThan(List<LocalDateTime> times, Duration maxGap) {
        if (times.size() < 2) return true;

        for (int i = 1; i < times.size(); i++) {
            Duration gap = Duration.between(times.get(i - 1), times.get(i));
            if (gap.compareTo(maxGap) < 0) {
                return false;
            }
        }
        return true;
    }

    public static String checkTriggerTimeAndParseJson(Set<LocalDateTime> triggerTime) {
        List<LocalDateTime> localDateTimes = checkTriggerInterval(triggerTime);
        List<String> pointInTimeDTOS = localDateTimes.stream()
                .map(time -> time.format(NORM_DATETIME_PATTERN)).toList();
        return JsonUtil.toJsonString(pointInTimeDTOS);
    }

}
