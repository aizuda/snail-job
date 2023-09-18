package com.aizuda.easy.retry.server.web.model.enums;

import com.aizuda.easy.retry.server.web.model.response.DispatchQuantityResponseVO;

import java.time.DayOfWeek;
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
     * 天
     */
    DAY(dispatchQuantityResponseVOList -> {
        Map<String, DispatchQuantityResponseVO> dispatchQuantityResponseVOMap = dispatchQuantityResponseVOList.stream().collect(Collectors.toMap(DispatchQuantityResponseVO::getCreateDt, i -> i));
        for (int i = 0; i < 24; i++) {

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH");
            String format = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(i).format(dateTimeFormatter);
            DispatchQuantityResponseVO dispatchQuantityResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dispatchQuantityResponseVO)) {
                dispatchQuantityResponseVO = new DispatchQuantityResponseVO();
                dispatchQuantityResponseVO.setFail(0L);
                dispatchQuantityResponseVO.setSuccess(0L);
                dispatchQuantityResponseVO.setCreateDt(format);
                dispatchQuantityResponseVOList.add(dispatchQuantityResponseVO);
            }
        }
    }, (startTime) -> {
        return LocalDateTime.of(startTime.toLocalDate(), LocalTime.MIN);
    }, (endTime) -> {
        return LocalDateTime.of(endTime.toLocalDate(), LocalTime.MAX);
    }),
    /**
     * 星期
     */
    WEEK(dispatchQuantityResponseVOList -> {
        Map<String, DispatchQuantityResponseVO> dispatchQuantityResponseVOMap = dispatchQuantityResponseVOList.stream().collect(Collectors.toMap(DispatchQuantityResponseVO::getCreateDt, i -> i));
        for (int i = 0; i < 7; i++) {

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String format = LocalDateTime.of(LocalDate.now().with(DayOfWeek.of(1)), LocalTime.MIN).plusDays(i).format(dateTimeFormatter);
            DispatchQuantityResponseVO dispatchQuantityResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dispatchQuantityResponseVO)) {
                dispatchQuantityResponseVO = new DispatchQuantityResponseVO();
                dispatchQuantityResponseVO.setFail(0L);
                dispatchQuantityResponseVO.setSuccess(0L);
                dispatchQuantityResponseVO.setCreateDt(format);
                dispatchQuantityResponseVOList.add(dispatchQuantityResponseVO);
            }
        }
    }, (startTime) -> {
        return LocalDateTime.of(startTime.toLocalDate().with(DayOfWeek.of(1)), LocalTime.MIN);
    }, (endTime) -> {
        return LocalDateTime.of(endTime.toLocalDate().with(DayOfWeek.of(7)), LocalTime.MAX);
    }),

    /**
     * 月
     */
    MONTH(dispatchQuantityResponseVOList -> {
        Map<String, DispatchQuantityResponseVO> dispatchQuantityResponseVOMap = dispatchQuantityResponseVOList.stream().collect(Collectors.toMap(DispatchQuantityResponseVO::getCreateDt, i -> i));
        int curMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        for (int i = 0; i < curMonth; i++) {

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String format = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN).plusDays(i).format(dateTimeFormatter);
            DispatchQuantityResponseVO dispatchQuantityResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dispatchQuantityResponseVO)) {
                dispatchQuantityResponseVO = new DispatchQuantityResponseVO();
                dispatchQuantityResponseVO.setFail(0L);
                dispatchQuantityResponseVO.setSuccess(0L);
                dispatchQuantityResponseVO.setCreateDt(format);
                dispatchQuantityResponseVOList.add(dispatchQuantityResponseVO);
            }
        }
    }, (startTime) -> {
        return LocalDateTime.of(startTime.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
    }, (endTime) -> {
        return LocalDateTime.of(endTime.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);
    }),

    /**
     * 年
     */
    YEAR(dispatchQuantityResponseVOList -> {
        Map<String, DispatchQuantityResponseVO> dispatchQuantityResponseVOMap = dispatchQuantityResponseVOList.stream().collect(Collectors.toMap(DispatchQuantityResponseVO::getCreateDt, i -> i));
        for (int i = 0; i < 12; i++) {

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
            String format = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN).plusMonths(i).format(dateTimeFormatter);
            DispatchQuantityResponseVO dispatchQuantityResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dispatchQuantityResponseVO)) {
                dispatchQuantityResponseVO = new DispatchQuantityResponseVO();
                dispatchQuantityResponseVO.setFail(0L);
                dispatchQuantityResponseVO.setSuccess(0L);
                dispatchQuantityResponseVO.setCreateDt(format);
                dispatchQuantityResponseVOList.add(dispatchQuantityResponseVO);
            }
        }
    }, (startTime) -> {
        return LocalDateTime.of(startTime.toLocalDate().with(TemporalAdjusters.firstDayOfYear()), LocalTime.MIN);
    }, (endTime) -> {
        return LocalDateTime.of(endTime.toLocalDate().with(TemporalAdjusters.lastDayOfYear()), LocalTime.MAX);
    }),

    /**
     * 其他类型
     */
    OTHERS(dispatchQuantityResponseVOList -> {
        Map<String, DispatchQuantityResponseVO> dispatchQuantityResponseVOMap = dispatchQuantityResponseVOList.stream().collect(Collectors.toMap(DispatchQuantityResponseVO::getCreateDt, i -> i));
        for (int i = 0; i < 24; i++) {

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");
            String format = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(i).format(dateTimeFormatter);
            DispatchQuantityResponseVO dispatchQuantityResponseVO = dispatchQuantityResponseVOMap.get(format);
            if (Objects.isNull(dispatchQuantityResponseVO)) {
                dispatchQuantityResponseVO = new DispatchQuantityResponseVO();
                dispatchQuantityResponseVO.setFail(0L);
                dispatchQuantityResponseVO.setSuccess(0L);
                dispatchQuantityResponseVO.setCreateDt(format);
                dispatchQuantityResponseVOList.add(dispatchQuantityResponseVO);
            }
        }
    }, (startTime) -> {
        if (Objects.isNull(startTime)) {
            return LocalDateTime.now();
        }
        return startTime;
    }, (endTime) -> {
        if (Objects.isNull(endTime)) {
            return LocalDateTime.now();
        }
        return endTime;
    });

    private Consumer<List<DispatchQuantityResponseVO>> consumer;
    private Function<LocalDateTime, LocalDateTime> startTime;
    private Function<LocalDateTime, LocalDateTime> endTime;

    DateTypeEnum(Consumer<List<DispatchQuantityResponseVO>> listConsumer, Function<LocalDateTime, LocalDateTime> startTime, Function<LocalDateTime, LocalDateTime> endTime) {
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

    public Consumer<List<DispatchQuantityResponseVO>> getConsumer() {
        return consumer;
    }
}
