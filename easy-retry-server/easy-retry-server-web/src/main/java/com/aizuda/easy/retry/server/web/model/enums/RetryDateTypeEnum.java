package com.aizuda.easy.retry.server.web.model.enums;

import com.aizuda.easy.retry.server.web.model.response.DashboardRetryLinkeResponseVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author: byteblogs
 * @date: 2020/1/19 20:36
 */
public enum RetryDateTypeEnum {
    /**
     * 天
     */
    DAY(dashboardRetryLinkeResponseVOList -> {
    }, (startTime) -> {
        return Objects.isNull(startTime) ?
                LocalDateTime.of(LocalDate.now(), LocalTime.MIN.withNano(0)) :
                LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return Objects.isNull(endTime) ?
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX.withNano(0)) :
                LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX.withNano(0));
    }),
    /**
     * 周
     */
    WEEK(dashboardRetryLinkeResponseVOList -> {
    }, (startTime) -> {
        return Objects.isNull(startTime) ?
                LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN.withNano(0)) :
                LocalDateTime.of(startTime.toLocalDate().minusDays(7), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return Objects.isNull(endTime) ?
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX.withNano(0)) :
                LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX.withNano(0));
    }),

    /**
     * 月
     */
    MONTH(dashboardRetryLinkeResponseVOList -> {
    }, (startTime) -> {
        return Objects.isNull(startTime) ?
                LocalDateTime.of(LocalDate.now().minusMonths(1), LocalTime.MIN.withNano(0)) :
                LocalDateTime.of(startTime.toLocalDate().minusMonths(1), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return Objects.isNull(endTime) ?
                LocalDateTime.of(LocalDate.now(), LocalTime.MAX.withNano(0)) :
                LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX.withNano(0));
    }),

    /**
     * 年
     */
    YEAR(dashboardRetryLinkeResponseVOList -> {
    }, (startTime) -> {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX.withNano(0));
    }),

    /**
     * 其他类型
     */
    OTHERS(dashboardRetryLinkeResponseVOList -> {
    }, (startTime) -> {
        return LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX.withNano(0));
    });

    private Consumer<List<DashboardRetryLinkeResponseVO>> consumer;
    private Function<LocalDateTime, LocalDateTime> startTime;
    private Function<LocalDateTime, LocalDateTime> endTime;

    RetryDateTypeEnum(Consumer<List<DashboardRetryLinkeResponseVO>> listConsumer, Function<LocalDateTime, LocalDateTime> startTime, Function<LocalDateTime, LocalDateTime> endTime) {
        this.consumer = listConsumer;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Function<LocalDateTime, LocalDateTime> getStartTime() {
        return startTime;
    }

    public Function<LocalDateTime, LocalDateTime> getEndTime() {
        return endTime;
    }

    public Consumer<List<DashboardRetryLinkeResponseVO>> getConsumer() {
        return consumer;
    }
}
