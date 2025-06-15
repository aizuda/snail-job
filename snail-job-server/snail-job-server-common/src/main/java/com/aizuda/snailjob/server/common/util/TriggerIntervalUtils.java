package com.aizuda.snailjob.server.common.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.PointInTimeDTO;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-06-14
 */
public class TriggerIntervalUtils {

    /**
     * 将 [yyyy-mm-dd HH:mm:ss] 格式转成 时间戳
     *
     * @param triggerInterval 触发间隔
     * @param triggerType     触发类型
     * @return [{time:1111}, {time:11231}]
     */
    public static String getPointInTimeStr(String triggerInterval, Integer triggerType) {
        if (StrUtil.isBlank(triggerInterval) || Objects.isNull(triggerType)) {
            return StrUtil.EMPTY;
        }

        if (triggerType.equals(WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType())) {
            List<String> pointInTimeDTOS = JsonUtil.parseList(triggerInterval, String.class);
            List<PointInTimeDTO> localDateTimes = pointInTimeDTOS
                    .stream()
                    .map(DateUtils::toLocalDateTime)
                    .map(DateUtils::toEpochMilli)
                    .map(time -> {
                        PointInTimeDTO pointInTimeDTO = new PointInTimeDTO();
                        pointInTimeDTO.setTime(time);
                        return pointInTimeDTO;
                    }).toList();
            return JsonUtil.toJsonString(localDateTimes);
        }

        return triggerInterval;
    }

    public static void checkTriggerInterval(String triggerInterval, Integer triggerType) {
        if (Objects.equals(triggerType, WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType())) {
            List<String> pointInTimeDTOS = JsonUtil.parseList(triggerInterval, String.class);
            LocalDateTime now = LocalDateTime.now();
            List<LocalDateTime> localDateTimes = pointInTimeDTOS
                    .stream()
                    .map(DateUtils::toLocalDateTime)
                    .sorted(Comparator.naturalOrder())
                    .toList();
            LocalDateTime first = localDateTimes.get(0);
            if (first.isBefore(now)) {
                throw new SnailJobServerException("The submission time is less than the current time. triggerTime:{} now:{}", first, now);
            }

            // 判断间隔是否大于10秒
            Assert.isTrue(areAllIntervalsLessThan(localDateTimes, Duration.ofSeconds(10)),
                    () -> new SnailJobServerException("There are combinations with intervals less than 10(s)"));
        }
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

}
