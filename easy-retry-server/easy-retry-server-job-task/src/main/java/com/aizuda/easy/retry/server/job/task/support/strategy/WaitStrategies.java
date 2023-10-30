package com.aizuda.easy.retry.server.job.task.support.strategy;

import com.aizuda.easy.retry.common.core.util.CronExpression;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.support.WaitStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

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
        /**
         * 触发类型 1.CRON 表达式 2. 固定时间
         */
        private Integer triggerType;

        /**
         * 间隔时长
         */
        private String triggerInterval;

        /**
         * 下次触发时间
         */
        private LocalDateTime nextTriggerAt;

    }

    @Getter
    @AllArgsConstructor
    public enum WaitStrategyEnum {
        CRON(1, cronWait()),
        FIXED(2, fixedWait()),
        ;

        private final int triggerType;
        private final WaitStrategy waitStrategy;


        /**
         * 获取退避策略
         *
         * @param triggerType 触发类型
         * @return 退避策略
         */
        public static WaitStrategy getWaitStrategy(int triggerType) {

            WaitStrategyEnum waitStrategy = getWaitStrategyEnum(triggerType);
            switch (waitStrategy) {
                case CRON:
                    return cronWait();
                case FIXED:
                    return fixedWait();
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
                if (value.triggerType == backOff) {
                    return value;
                }
            }

            // TODO 兜底为默认等级策略
            return null;
        }

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
     * 固定定时间等待策略
     */
    private static final class FixedWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(WaitStrategyContext context) {
            long triggerInterval = Long.parseLong(context.triggerInterval);

            LocalDateTime nextTriggerAt = context.getNextTriggerAt();
            if (nextTriggerAt.isBefore(LocalDateTime.now())) {
                nextTriggerAt = LocalDateTime.now();
            }

            return nextTriggerAt.plusSeconds(triggerInterval);
        }
    }

    /**
     * Cron等待策略
     */
    private static final class CronWaitStrategy implements WaitStrategy {

        @Override
        public LocalDateTime computeRetryTime(WaitStrategyContext context) {

            LocalDateTime nextTriggerAt = context.getNextTriggerAt();
            if (nextTriggerAt.isBefore(LocalDateTime.now())) {
                nextTriggerAt = LocalDateTime.now();
            }

            Date nextValidTime;
            try {
                ZonedDateTime zdt = nextTriggerAt.atZone(ZoneOffset.ofHours(8));
                nextValidTime = new CronExpression(context.getTriggerInterval()).getNextValidTimeAfter(Date.from(zdt.toInstant()));
            } catch (ParseException e) {
                throw new EasyRetryServerException("解析CRON表达式异常 [{}]", context.getTriggerInterval(), e);
            }

            return LocalDateTime.ofEpochSecond( nextValidTime.getTime() / 1000,0, ZoneOffset.ofHours(8));
        }
    }

}
