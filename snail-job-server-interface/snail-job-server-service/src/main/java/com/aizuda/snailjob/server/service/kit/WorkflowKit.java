package com.aizuda.snailjob.server.service.kit;

import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.google.common.collect.Lists;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-24
 */
public class WorkflowKit {

    public static Long calculateNextTriggerAt(Integer triggerType, String triggerInterval, Long time) {
        checkExecuteInterval(triggerType, triggerInterval);

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(triggerType);
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(triggerInterval);
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }


    public static void checkExecuteInterval(Integer triggerType, String triggerInterval) {
        if (Lists.newArrayList(WaitStrategies.WaitStrategyEnum.FIXED.getType(),
                        WaitStrategies.WaitStrategyEnum.RANDOM.getType())
                .contains(triggerType)) {
            if (Integer.parseInt(triggerInterval) < 10) {
                throw new SnailJobServerException("Trigger interval must not be less than 10");
            }
        } else if (Objects.equals(triggerType, WaitStrategies.WaitStrategyEnum.CRON.getType())) {
            if (CronUtils.getExecuteInterval(triggerInterval) < 10 * 1000) {
                throw new SnailJobServerException("Trigger interval must not be less than 10");
            }
        }
    }
}
