package com.aizuda.snailjob.server.retry.task.support.dispatch.actor.scan;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author opensnail
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.SCAN_RETRY_GROUP_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScanRetryTaskActor extends AbstractScanGroup {

    private static final AtomicLong preCostTime = new AtomicLong(0L);
    private static final AtomicLong pullCount = new AtomicLong(1L);

    /**
     * 缓存待拉取数据的起点id
     * <p>
     * LAST_AT_MAP[key] = groupName  LAST_AT_MAP[value] = retry_task的id
     */
    private static final ConcurrentMap<String, Long> LAST_AT_MAP = new ConcurrentHashMap<>();

    @Override
    protected TaskExecutorSceneEnum taskActuatorScene() {
        return TaskExecutorSceneEnum.AUTO_RETRY;
    }

    @Override
    protected Long getLastId(final String groupName) {
        return LAST_AT_MAP.get(groupName);
    }

    @Override
    protected void putLastId(final String groupName, final Long lastId) {
        LAST_AT_MAP.put(groupName, lastId);
    }

    @Override
    protected LocalDateTime calculateNextTriggerTime(RetryPartitionTask partitionTask, final RetrySceneConfig retrySceneConfig) {
        // 更新下次触发时间

        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTriggerAt = partitionTask.getNextTriggerAt();
        if (nextTriggerAt.plusSeconds(SystemConstants.SCHEDULE_PERIOD).isBefore(now)) {
            nextTriggerAt = now;
        }

        waitStrategyContext.setNextTriggerAt(DateUtils.toEpochMilli(nextTriggerAt));
        waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
        waitStrategyContext.setDelayLevel(partitionTask.getRetryCount() + 1);
        // 更新触发时间, 任务进入时间轮
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff());
        return DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext));
    }

    @Override
    protected TimerTask timerTask(final RetryPartitionTask partitionTask) {
        RetryTimerContext retryTimerContext = RetryTaskConverter.INSTANCE.toRetryTimerContext(partitionTask);
        retryTimerContext.setScene(taskActuatorScene());
        return new RetryTimerTask(retryTimerContext);
    }

    @Override
    protected AtomicLong preCostTime() {
        return preCostTime;
    }

    @Override
    protected AtomicLong prePullCount() {
        return pullCount;
    }
}
