package com.aizuda.snailjob.server.service.kit;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.snailjob.server.service.dto.CalculateNextTriggerAtDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
public final class JobKit {

    /**
     * 判断是否是常驻任务
     */
    public static Integer isResident(Integer triggerType, String triggerInterval) {
        if (Objects.equals(triggerType, SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return StatusEnum.NO.getStatus();
        }

        if (Objects.equals(triggerType, WaitStrategies.WaitStrategyEnum.FIXED.getType())) {
            if (Integer.parseInt(triggerInterval) < 10) {
                return StatusEnum.YES.getStatus();
            }
        } else if (Objects.equals(triggerType, WaitStrategies.WaitStrategyEnum.CRON.getType())) {
            if (CronUtils.getExecuteInterval(triggerInterval) < 10 * 1000) {
                return StatusEnum.YES.getStatus();
            }
        } else if (Objects.equals(triggerType, WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType())) {
            return StatusEnum.NO.getStatus();
        } else {
            throw new SnailJobServerException("Unknown trigger type");
        }

        return StatusEnum.NO.getStatus();
    }

    public static Long calculateNextTriggerAt(CalculateNextTriggerAtDTO triggerAtDTO) {
        Integer triggerType = triggerAtDTO.getTriggerType();
        String triggerInterval = triggerAtDTO.getTriggerInterval();
        Long nextTriggerAt = 0L;
        // 工作流任务
        if (Objects.equals(triggerAtDTO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            nextTriggerAt = 0L;
            // 非常驻任务 > 非常驻任务
        } else if (Objects.equals(triggerAtDTO.getOldResident(), StatusEnum.NO.getStatus()) && Objects.equals(
                triggerAtDTO.getNewResident(), StatusEnum.NO.getStatus())) {
            nextTriggerAt = calculateNextTriggerAt(triggerType, triggerInterval, DateUtils.toNowMilli());
        } else if (Objects.equals(triggerAtDTO.getOldResident(), StatusEnum.YES.getStatus()) && Objects.equals(
                triggerAtDTO.getNewResident(), StatusEnum.NO.getStatus())) {
            // 常驻任务的触发时间
            long time = Optional.ofNullable(ResidentTaskCache.get(triggerAtDTO.getId()))
                    .orElse(DateUtils.toNowMilli());
            nextTriggerAt = calculateNextTriggerAt(triggerType, triggerInterval, time);
            // 老的是不是常驻任务 新的是常驻任务 需要使用当前时间计算下次触发时间
        } else if (Objects.equals(triggerAtDTO.getOldResident(), StatusEnum.NO.getStatus()) && Objects.equals(
                triggerAtDTO.getNewResident(), StatusEnum.YES.getStatus())) {
            nextTriggerAt = DateUtils.toNowMilli();
        }

        return nextTriggerAt;
    }

    public static Long calculateNextTriggerAt(Integer triggerType, String triggerInterval, Long time) {
        if (Objects.equals(triggerType, SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return 0L;
        }

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(triggerType);
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(triggerInterval);
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }
}
