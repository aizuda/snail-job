package com.aizuda.snailjob.server.web.model.enums;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.web.model.response.DashboardLineResponseVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author: byteblogs
 * @date: 2020/1/19 20:36
 */
public enum DateTypeEnum {
    /**
     * 天（按小时）
     */
    DAY(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(voList,
                        DashboardLineResponseVO::getCreateDt);
                int hourNow = LocalDateTime.now().getHour();
                for (int hourOffset = 0; hourOffset <= hourNow; hourOffset++) {
                    String createDt = LocalDateTime.now().plusHours(hourOffset).format(DateTimeFormatter.ofPattern("HH"));
                    if (!responseVoMap.containsKey(createDt)) {
                        voList.add(buildZeroedVoWithCreateDt(createDt));
                    }
                }
            },
            (startTime) -> LocalDateTimeUtil.beginOfDay(startTime),
            (endTime) -> LocalDateTimeUtil.endOfDay(endTime)
    ),

    /**
     * 周
     */
    WEEK(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(
                        voList, DashboardLineResponseVO::getCreateDt);
                for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                    String createDt = LocalDateTime.now().minusDays(dayOffset).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (!responseVoMap.containsKey(createDt)) {
                        voList.add(buildZeroedVoWithCreateDt(createDt));
                    }
                }
            },
            (startTime) -> LocalDateTimeUtil.beginOfDay(startTime).minusDays(7),
            (endTime) -> LocalDateTimeUtil.endOfDay(endTime)
    ),

    /**
     * 月
     */
    MONTH(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(
                        voList, DashboardLineResponseVO::getCreateDt);
                int lastDayOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
                for (int dayOffset = 0; dayOffset < lastDayOfMonth; dayOffset++) {
                    String createDt = LocalDate.now().minusDays(dayOffset).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    if (!responseVoMap.containsKey(createDt)) {
                        voList.add(buildZeroedVoWithCreateDt(createDt));
                    }
                }
            },
            (startTime) -> LocalDateTimeUtil.beginOfDay(startTime).minusMonths(1),
            (endTime) -> LocalDateTimeUtil.endOfDay(endTime)
    ),

    /**
     * 年
     */
    YEAR(
            voList -> {
                Map<String, DashboardLineResponseVO> responseVoMap = StreamUtils.toIdentityMap(
                        voList, DashboardLineResponseVO::getCreateDt);
                for (int monthOffset = 0; monthOffset < 12; monthOffset++) {
                    String createDt = LocalDateTime.now().minusMonths(monthOffset).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    if (!responseVoMap.containsKey(createDt)) {
                        voList.add(buildZeroedVoWithCreateDt(createDt));
                    }
                }
            },
            (startTime) -> LocalDateTimeUtil.beginOfDay(LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear())),
            (endTime) -> LocalDateTimeUtil.endOfDay(LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear()))
    ),

    /**
     * 其他类型
     */
    OTHERS(
            voList -> {
            },
            (startTime) -> LocalDateTimeUtil.beginOfDay(startTime),
            (endTime) -> LocalDateTimeUtil.endOfDay(endTime));

    private Consumer<List<DashboardLineResponseVO>> consumer;
    private Function<LocalDateTime, LocalDateTime> startTime;
    private Function<LocalDateTime, LocalDateTime> endTime;

    DateTypeEnum(Consumer<List<DashboardLineResponseVO>> consumer,
                 Function<LocalDateTime, LocalDateTime> startTime,
                 Function<LocalDateTime, LocalDateTime> endTime) {
        this.consumer = consumer;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private static DashboardLineResponseVO buildZeroedVoWithCreateDt(String createDt) {
        return new DashboardLineResponseVO()
                .setTotal(0L)
                .setTotalNum(0L)
                .setFail(0L)
                .setFailNum(0L)
                .setMaxCountNum(0L)
                .setRunningNum(0L)
                .setSuccess(0L)
                .setSuccessNum(0L)
                .setSuspendNum(0L)
                .setStop(0L)
                .setCancel(0L)
                .setCreateDt(createDt);
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
