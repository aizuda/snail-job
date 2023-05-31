package com.aizuda.easy.retry.server.support.strategy;

import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.SceneConfig;
import com.aizuda.easy.retry.server.persistence.support.ConfigAccess;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.DelayLevelEnum;
import com.aizuda.easy.retry.common.core.util.CronExpression;
import com.google.common.base.Preconditions;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.WaitStrategy;
import com.aizuda.easy.retry.server.support.context.MaxAttemptsPersistenceRetryContext;
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
@SuppressWarnings({"squid:S3776", "squid:S2676", "squid:S3740"})
public class WaitStrategies {

    private WaitStrategies() {
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

        @Data
        public static class StrategyParameter {
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
    private static final  class DelayLevelWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;
            RetryTask retryTask = context.getRetryTask();
            DelayLevelEnum levelEnum = DelayLevelEnum.getDelayLevelByLevel(retryTask.getRetryCount());
            return retryTask.getNextTriggerAt().plus(levelEnum.getTime(), levelEnum.getUnit());
        }
    }

    /**
     * 固定定时间等待策略
     */
    private static final class FixedWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;
            RetryTask retryTask = context.getRetryTask();
            ConfigAccess configAccess = SpringContext.CONTEXT.getBean("configAccessProcessor", ConfigAccess.class);

            SceneConfig sceneConfig =
                    configAccess.getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName());
            return retryTask.getNextTriggerAt().plusSeconds(Integer.parseInt(sceneConfig.getTriggerInterval()));
        }
    }

    /**
     * Cron等待策略
     */
    private static final class CronWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(RetryContext retryContext) {
            MaxAttemptsPersistenceRetryContext context = (MaxAttemptsPersistenceRetryContext) retryContext;
            RetryTask retryTask = context.getRetryTask();

            ConfigAccess configAccess = SpringContext.CONTEXT.getBean(ConfigAccess.class);

            SceneConfig sceneConfig =
                    configAccess.getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName());

            Date nextValidTime = null;
            try {
                ZonedDateTime zdt = retryTask.getNextTriggerAt().atZone(ZoneOffset.ofHours(8));
                nextValidTime = new CronExpression(sceneConfig.getTriggerInterval()).getNextValidTimeAfter(Date.from(zdt.toInstant()));
            } catch (ParseException e) {
                throw new EasyRetryServerException("解析CRON表达式异常 [{}]", sceneConfig.getTriggerInterval(), e);
            }

            return  LocalDateTime.ofEpochSecond( nextValidTime.getTime() / 1000,0, ZoneOffset.ofHours(8));
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
        public LocalDateTime computeRetryTime(RetryContext retryContext) {

            if (Objects.nonNull(retryContext)) {
                RetryTask retryTask = retryContext.getRetryTask();

                ConfigAccess configAccess = SpringContext.CONTEXT.getBean(ConfigAccess.class);
                SceneConfig sceneConfig =
                        configAccess.getSceneConfigByGroupNameAndSceneName(retryTask.getGroupName(), retryTask.getSceneName());

                if (maximum == 0) {
                    maximum = Long.parseLong(sceneConfig.getTriggerInterval());
                }

            }

            Preconditions.checkArgument(maximum > minimum, "maximum must be > minimum but maximum is %d and minimum is", maximum, minimum);

            long t = Math.abs(RANDOM.nextLong()) % (maximum - minimum);
            long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();

            return  LocalDateTime.ofEpochSecond( (t + minimum + now) / 1000,0, ZoneOffset.ofHours(8));
        }
    }
}
