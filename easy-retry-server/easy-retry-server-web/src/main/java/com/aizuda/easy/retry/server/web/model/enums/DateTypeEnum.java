package com.aizuda.easy.retry.server.web.model.enums;

import com.aizuda.easy.retry.server.web.model.response.DashboardLineResponseVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: byteblogs
 * @date: 2020/1/19 20:36
 */
public enum DateTypeEnum {
    /**
     * 天（按小时）
     */
    DAY(dashboardLineResponseVOList -> {
        Map<String, DashboardLineResponseVO> dashboardLineResponseVOMap = dashboardLineResponseVOList.stream().collect(Collectors.toMap(DashboardLineResponseVO::getCreateDt, i -> i));
        for (int i = 0; i <= LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).getHour(); i++) {

            String format = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(i).format(DateTimeFormatter.ofPattern("HH"));
            DashboardLineResponseVO dashboardLineResponseVO = dashboardLineResponseVOMap.get(format);
            if (Objects.isNull(dashboardLineResponseVO)) {
                dashboardLineResponseVO = new DashboardLineResponseVO()
                        .setTotal(0L)
                        .setTotalNum(0L)
                        .setFail(0L)
                        .setFailNum(0L)
                        .setMaxCountNum(0L)
                        .setRunningNum(0L)
                        .setSuccessNum(0L)
                        .setSuspendNum(0L)
                        .setStopNum(0L)
                        .setCancelNum(0L)
                        .setCreateDt(format);
                dashboardLineResponseVOList.add(dashboardLineResponseVO);
            }
        }
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
    WEEK(dashboardLineResponseVOList -> {
        Map<String, DashboardLineResponseVO> dispatchQuantityResponseVOMap = dashboardLineResponseVOList.stream().collect(Collectors.toMap(DashboardLineResponseVO::getCreateDt, i -> i));
        for (int i = 0; i < 7; i++) {

            String format = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DashboardLineResponseVO dashboardLineResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dashboardLineResponseVO)) {
                dashboardLineResponseVO = new DashboardLineResponseVO()
                        .setTotal(0L)
                        .setTotalNum(0L)
                        .setFail(0L)
                        .setFailNum(0L)
                        .setMaxCountNum(0L)
                        .setRunningNum(0L)
                        .setSuccessNum(0L)
                        .setSuspendNum(0L)
                        .setStopNum(0L)
                        .setCancelNum(0L)
                        .setCreateDt(format);
                dashboardLineResponseVOList.add(dashboardLineResponseVO);
            }
        }
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
    MONTH(dashboardLineResponseVOList -> {
        Map<String, DashboardLineResponseVO> dispatchQuantityResponseVOMap = dashboardLineResponseVOList.stream().collect(Collectors.toMap(DashboardLineResponseVO::getCreateDt, i -> i));
        for (int i = 0; i < LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth(); i++) {

            String format = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            DashboardLineResponseVO dashboardLineResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dashboardLineResponseVO)) {
                dashboardLineResponseVO = new DashboardLineResponseVO()
                        .setTotal(0L)
                        .setTotalNum(0L)
                        .setFail(0L)
                        .setFailNum(0L)
                        .setMaxCountNum(0L)
                        .setRunningNum(0L)
                        .setSuccessNum(0L)
                        .setSuspendNum(0L)
                        .setStopNum(0L)
                        .setCancelNum(0L)
                        .setCreateDt(format);
                dashboardLineResponseVOList.add(dashboardLineResponseVO);
            }
        }
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
    YEAR(dashboardLineResponseVOList -> {
    }, (startTime) -> {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX.withNano(0));
    }),

    /**
     * 其他类型
     */
    OTHERS(dashboardLineResponseVOList -> {
    }, (startTime) -> {
        return LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN.withNano(0));
    }, (endTime) -> {
        return LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX.withNano(0));
    });

    private Consumer<List<DashboardLineResponseVO>> consumer;
    private Function<LocalDateTime, LocalDateTime> startTime;
    private Function<LocalDateTime, LocalDateTime> endTime;

    DateTypeEnum(Consumer<List<DashboardLineResponseVO>> listConsumer, Function<LocalDateTime, LocalDateTime> startTime, Function<LocalDateTime, LocalDateTime> endTime) {
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

    public Consumer<List<DashboardLineResponseVO>> getConsumer() {
        return consumer;
    }
}
