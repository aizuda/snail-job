package com.aizuda.snailjob.server.service.kit;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;

import java.util.Objects;

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
}
