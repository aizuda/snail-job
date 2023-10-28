package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.scan;

import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.easy.retry.server.retry.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.server.retry.task.support.timer.RetryTimerContext;
import com.aizuda.easy.retry.server.retry.task.support.timer.RetryTimerTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author www.byteblogs.com
 * @date 2021-10-30
 * @since 2.0
 */
@Component(ActorGenerator.SCAN_RETRY_GROUP_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScanRetryTaskActor extends AbstractScanGroup {

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

    protected LocalDateTime calculateNextTriggerTime(RetryPartitionTask partitionTask) {
        // 更新下次触发时间
        SceneConfig sceneConfig = accessTemplate.getSceneConfigAccess()
            .getSceneConfigByGroupNameAndSceneName(partitionTask.getGroupName(), partitionTask.getSceneName());

        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setNextTriggerAt(partitionTask.getNextTriggerAt());
        waitStrategyContext.setTriggerInterval(sceneConfig.getTriggerInterval());
        waitStrategyContext.setTriggerCount(partitionTask.getRetryCount() + 1);
        // 更新触发时间, 任务进入时间轮
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(sceneConfig.getBackOff());
        return waitStrategy.computeRetryTime(waitStrategyContext);
    }

    @Override
    protected TimerTask timerTask(final RetryPartitionTask partitionTask) {
        RetryTimerContext retryTimerContext = new RetryTimerContext();
        retryTimerContext.setGroupName(partitionTask.getGroupName());
        retryTimerContext.setScene(taskActuatorScene());
        retryTimerContext.setUniqueId(partitionTask.getUniqueId());
        return new RetryTimerTask(retryTimerContext);
    }
}