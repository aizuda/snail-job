package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.scan;

import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.server.retry.task.support.timer.CallbackTimerTask;
import com.aizuda.easy.retry.server.retry.task.support.timer.RetryTimerContext;
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
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 1.5.0
 */
@Component(ActorGenerator.SCAN_CALLBACK_GROUP_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScanCallbackTaskActor extends AbstractScanGroup {

    private static final AtomicLong preCostTime = new AtomicLong(0L);
    private static final AtomicLong pullCount = new AtomicLong(1L);

    /**
     * 缓存待拉取数据的起点id
     * <p>
     * LAST_AT_MAP[key] = groupName  LAST_AT_MAP[value] = retry_task的 id
     */
    private static final ConcurrentMap<String, Long> LAST_AT_MAP = new ConcurrentHashMap<>();

    @Override
    protected TaskExecutorSceneEnum taskActuatorScene() {
        return TaskExecutorSceneEnum.AUTO_CALLBACK;
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
    protected LocalDateTime calculateNextTriggerTime(final RetryPartitionTask partitionTask) {

        long triggerInterval = systemProperties.getCallback().getTriggerInterval();
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(WaitStrategyEnum.FIXED.getType());
        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(partitionTask.getNextTriggerAt());
        waitStrategyContext.setTriggerInterval(String.valueOf(triggerInterval));

        // 更新触发时间, 任务进入时间轮
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    @Override
    protected TimerTask timerTask(final RetryPartitionTask partitionTask) {
        RetryTimerContext retryTimerContext = new RetryTimerContext();
        retryTimerContext.setGroupName(partitionTask.getGroupName());
        retryTimerContext.setScene(taskActuatorScene());
        retryTimerContext.setUniqueId(partitionTask.getUniqueId());

        return new CallbackTimerTask(retryTimerContext);
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
