package com.aizuda.easy.retry.server.retry.task.support.strategy;

import cn.hutool.core.date.DateUtil;
import com.aizuda.easy.retry.common.core.util.CronExpression;
import com.aizuda.easy.retry.server.common.enums.DelayLevelEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.retry.task.support.WaitStrategy;
import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.Getter;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 生成 {@link WaitStrategy} 实例.
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:19
 */
public class WaitStrategies {

    private WaitStrategies() {
    }

    @Data
    public static class WaitStrategyContext {
//        /**
//         * 触发类型 1.CRON 表达式 2. 固定时间
//         */
//        private Integer triggerType;

        /**
         * 间隔时长
         */
        private String triggerInterval;

        /**
         * 下次触发时间
         */
        private LocalDateTime nextTriggerAt;

        /**
         * 触发次数
         */
        private Integer triggerCount;

    }

    @Getter
    public enum WaitStrategyEnum {
        DELAY_LEVEL(1, delayLevelWait()),
        FIXED(2, fixedWait()),
        CRON(3, cronWait()),
        RANDOM(4, randomWait());

        private final int backOff;
        private final WaitStrategy waitStrategy;

        WaitStrategyEnum(int backOff, WaitStrategy waitStrategy) {
            this.backOff = backOff;
            this.waitStrategy = waitStrategy;
        }

        /**
         * 获取退避策略
         *
         * @param backOff 退避策略
         * @return 退避策略
         */
        public static WaitStrategy getWaitStrategy(int backOff) {

            WaitStrategyEnum waitStrategy = getWaitStrategyEnum(backOff);
            switch (waitStrategy) {
                case RANDOM:
                    return randomWait();
                case CRON:
                    return cronWait();
                default:
                   return waitStrategy.waitStrategy;
            }

        }

        /**
         * 获取退避策略枚举对象
         *
         * @param backOff 退避策略
         * @return 退避策略枚举对象
         */
        public static WaitStrategyEnum getWaitStrategyEnum(int backOff) {

            for (WaitStrategyEnum value : WaitStrategyEnum.values()) {
                if (value.backOff == backOff) {
                    return value;
                }
            }

            // 兜底为默认等级策略
            return WaitStrategyEnum.DELAY_LEVEL;
        }

    }

    /**
     * 延迟等级等待策略
     *
     * @return {@link DelayLevelWaitStrategy} 延迟等级等待策略
     */
    public static WaitStrategy delayLevelWait() {
        return new DelayLevelWaitStrategy();
    }

    /**
     * 固定定时间等待策略
     *
     * @return {@link FixedWaitStrategy} 固定定时间等待策略
     */
    public static WaitStrategy fixedWait() {
        return new FixedWaitStrategy();
    }

    /**
     * cron等待策略
     *
     * @return {@link CronWaitStrategy} cron等待策略
     */
    public static WaitStrategy cronWait() {
        return new CronWaitStrategy();
    }

    /**
     * 随机等待策略
     *
     * @return {@link RandomWaitStrategy} 随机等待策略
     */
    public static WaitStrategy randomWait(long minimumTime, TimeUnit minimumTimeUnit, long maximumTime, TimeUnit maximumTimeUnit) {
        return new RandomWaitStrategy(minimumTimeUnit.toMillis(minimumTime), maximumTimeUnit.toMillis(maximumTime));
    }

    /**
     * 随机等待策略
     *
     * @return {@link RandomWaitStrategy} 随机等待策略
     */
    public static WaitStrategy randomWait() {
        return new RandomWaitStrategy();
    }

    /**
     * 延迟等级等待策略
     */
    private static final class DelayLevelWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(WaitStrategyContext context) {
            DelayLevelEnum levelEnum = DelayLevelEnum.getDelayLevelByLevel(context.getTriggerCount());
            return context.getNextTriggerAt().plus(levelEnum.getTime(), levelEnum.getUnit());
        }
    }

    /**
     * 固定定时间等待策略
     */
    private static final class FixedWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(WaitStrategyContext retryContext) {
            return retryContext.getNextTriggerAt().plusSeconds(Integer.parseInt(retryContext.getTriggerInterval()));
        }
    }

    /**
     * Cron等待策略
     */
    private static final class CronWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(WaitStrategyContext context) {

            try {
                ZonedDateTime zdt = context.getNextTriggerAt().atZone(ZoneOffset.ofHours(8));
                Date nextValidTime = new CronExpression(context.getTriggerInterval()).getNextValidTimeAfter(Date.from(zdt.toInstant()));
                return DateUtil.toLocalDateTime(nextValidTime);
            } catch (ParseException e) {
                throw new EasyRetryServerException("解析CRON表达式异常 [{}]", context.getTriggerInterval(), e);
            }

        }
    }

    /**
     * 随机等待策略
     */
    private static final class RandomWaitStrategy implements WaitStrategy {

        private static final Random RANDOM = new Random();
        private final long minimum;
        private long maximum;

        public RandomWaitStrategy(long minimum, long maximum) {
            Preconditions.checkArgument(minimum >= 0, "minimum must be >= 0 but is %d", minimum);
            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            this.minimum = minimum;
            this.maximum = maximum;
        }

        public RandomWaitStrategy() {
            this.minimum = 0;
        }

        @Override
        public LocalDateTime computeRetryTime(WaitStrategyContext retryContext) {

            if (Objects.nonNull(retryContext)) {
                if (maximum == 0) {
                    maximum = Long.parseLong(retryContext.getTriggerInterval());
                }
            }

            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            long t = Math.abs(RANDOM.nextLong()) % (maximum - minimum);
            long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();

            return  LocalDateTime.ofEpochSecond( (t + minimum + now) / 1000,0, ZoneOffset.ofHours(8));
        }
    }
}
