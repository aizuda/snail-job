package com.aizuda.snailjob.server.retry.task.support.dispatch.actor.scan;

import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.snailjob.server.retry.task.support.timer.CallbackTimerTask;
import com.aizuda.snailjob.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.snailjob.template.datasource.persistence.po.SceneConfig;
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
    protected LocalDateTime calculateNextTriggerTime(final RetryPartitionTask partitionTask,
        final SceneConfig sceneConfig) {

        long triggerInterval = systemProperties.getCallback().getTriggerInterval();
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(WaitStrategyEnum.FIXED.getType());
        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(partitionTask.getNextTriggerAt());
        waitStrategyContext.setTriggerInterval(String.valueOf(triggerInterval));

        // 更新触发时间, 任务进入时间轮
        return DateUtils.toLocalDateTime(waitStrategy.computeTriggerTime(waitStrategyContext));
    }

    @Override
    protected TimerTask timerTask(final RetryPartitionTask partitionTask) {
        RetryTimerContext retryTimerContext = RetryTaskConverter.INSTANCE.toRetryTimerContext(partitionTask);
        retryTimerContext.setScene(taskActuatorScene());
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
