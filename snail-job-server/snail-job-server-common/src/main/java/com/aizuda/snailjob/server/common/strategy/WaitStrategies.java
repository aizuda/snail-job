package com.aizuda.snailjob.server.common.strategy;

import com.aizuda.snailjob.common.core.exception.SnailJobCommonException;
import com.aizuda.snailjob.common.core.util.CronExpression;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.enums.DelayLevelEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 生成 {@link WaitStrategy} 实例.
 *
 * @author: opensnail
 * @date : 2021-11-29 18:19
 */
public class WaitStrategies {

    private WaitStrategies() {
    }

    @Data
    public static class WaitStrategyContext {

        /**
         * 间隔时长
         */
        private String triggerInterval;

        /**
         * 下次触发时间
         */
        private long nextTriggerAt;

        /**
         * 延迟等级
         * 仅在选择 DELAY_LEVEL时使用 {@link DelayLevelEnum}
         */
        private Integer delayLevel;

        public void setNextTriggerAt(final long nextTriggerAt) {
            this.nextTriggerAt = nextTriggerAt;
        }

        public void setNextTriggerAt(final LocalDateTime nextTriggerAt) {
            this.nextTriggerAt = DateUtils.toEpochMilli(nextTriggerAt);
        }
    }

    @AllArgsConstructor
    @Getter
    public enum WaitStrategyEnum {
        DELAY_LEVEL(1, delayLevelWait()),
        FIXED(2, fixedWait()),
        CRON(3, cronWait()),
        RANDOM(4, randomWait());

        private final int type;
        private final WaitStrategy waitStrategy;

        /**
         * 获取退避策略
         *
         * @param backOff 退避策略
         * @return 退避策略
         */
        public static WaitStrategy getWaitStrategy(int backOff) {
            return getWaitStrategyEnum(backOff).getWaitStrategy();

        }

        /**
         * 获取等待策略类型枚举对象
         *
         * @param type 等待策略类型
         * @return 等待策略类型枚举对象
         */
        public static WaitStrategyEnum getWaitStrategyEnum(int type) {

            for (WaitStrategyEnum value : WaitStrategyEnum.values()) {
                if (value.type == type) {
                    return value;
                }
            }

            // 兜底为默认等级策略
            throw new SnailJobCommonException("等待策略类型不存在. [{}]", type);
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
        public Long computeTriggerTime(WaitStrategyContext context) {
            DelayLevelEnum levelEnum = DelayLevelEnum.getDelayLevelByLevel(context.getDelayLevel());
            Duration of = Duration.of(levelEnum.getTime(), levelEnum.getUnit());
            return context.getNextTriggerAt() + of.toMillis();
        }
    }

    /**
     * 固定定时间等待策略
     */
    private static final class FixedWaitStrategy implements WaitStrategy {

        @Override
        public Long computeTriggerTime(WaitStrategyContext retryContext) {
            return retryContext.getNextTriggerAt() + DateUtils.toEpochMilli(Integer.parseInt(retryContext.getTriggerInterval()));
        }
    }

    /**
     * Cron等待策略
     */
    private static final class CronWaitStrategy implements WaitStrategy {

        @Override
        public Long computeTriggerTime(WaitStrategyContext context) {

            try {
                Date nextValidTime = new CronExpression(context.getTriggerInterval()).getNextValidTimeAfter(new Date(context.getNextTriggerAt()));
                return DateUtils.toEpochMilli(nextValidTime);
            } catch (ParseException e) {
                throw new SnailJobServerException("解析CRON表达式异常 [{}]", context.getTriggerInterval(), e);
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
        public Long computeTriggerTime(WaitStrategyContext retryContext) {

            if (Objects.nonNull(retryContext)) {
                if (maximum == 0) {
                    maximum = Long.parseLong(retryContext.getTriggerInterval());
                }
            }

            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            long t = Math.abs(RANDOM.nextLong()) % (maximum - minimum);
            return (t + minimum + DateUtils.toNowMilli());
        }
    }
}
