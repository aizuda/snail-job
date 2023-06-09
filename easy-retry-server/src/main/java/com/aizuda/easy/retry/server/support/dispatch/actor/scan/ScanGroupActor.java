package com.aizuda.easy.retry.server.support.dispatch.actor.scan;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.persistence.mybatis.po.RetryTask;
import com.aizuda.easy.retry.server.persistence.mybatis.po.SceneConfig;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.WaitStrategy;
import com.aizuda.easy.retry.server.support.context.MaxAttemptsPersistenceRetryContext;
import com.aizuda.easy.retry.server.support.retry.RetryBuilder;
import com.aizuda.easy.retry.server.support.retry.RetryExecutor;
import com.aizuda.easy.retry.server.support.strategy.FilterStrategies;
import com.aizuda.easy.retry.server.support.strategy.StopStrategies;
import com.aizuda.easy.retry.server.support.strategy.WaitStrategies;
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
@Component(ScanGroupActor.BEAN_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScanGroupActor extends AbstractScanGroup {

    public static final String BEAN_NAME = "ScanGroupActor";
    /**
     * 缓存待拉取数据的起点时间
     * <p>
     * LAST_AT_MAP[key] = groupName  LAST_AT_MAP[value] = retry_task的 create_at时间
     */
    public static final ConcurrentMap<String, LocalDateTime> LAST_AT_MAP = new ConcurrentHashMap<>();

    @Override
    protected RetryContext<Result<DispatchRetryResultDTO>> builderRetryContext(final String groupName,
        final RetryTask retryTask) {
        MaxAttemptsPersistenceRetryContext<Result<DispatchRetryResultDTO>> retryContext = new MaxAttemptsPersistenceRetryContext<>();
        retryContext.setRetryTask(retryTask);
        retryContext.setSceneBlacklist(configAccess.getBlacklist(groupName));
        retryContext.setServerNode(clientNodeAllocateHandler.getServerNode(retryTask.getGroupName()));
        return retryContext;
    }

    @Override
    protected RetryExecutor<Result<DispatchRetryResultDTO>> builderResultRetryExecutor(RetryContext retryContext) {

        RetryTask retryTask = retryContext.getRetryTask();
        return RetryBuilder.<Result<DispatchRetryResultDTO>>newBuilder()
            .withStopStrategy(StopStrategies.stopException())
            .withStopStrategy(StopStrategies.stopResultStatusCode())
            .withWaitStrategy(getWaitWaitStrategy(retryTask.getGroupName(), retryTask.getSceneName()))
            .withFilterStrategy(FilterStrategies.delayLevelFilter())
            .withFilterStrategy(FilterStrategies.bitSetIdempotentFilter(idempotentStrategy))
            .withFilterStrategy(FilterStrategies.sceneBlackFilter())
            .withFilterStrategy(FilterStrategies.checkAliveClientPodFilter())
            .withFilterStrategy(FilterStrategies.rebalanceFilterStrategies())
            .withFilterStrategy(FilterStrategies.rateLimiterFilter())
            .withRetryContext(retryContext)
            .build();
    }

    @Override
    protected Integer getTaskType() {
        return TaskTypeEnum.RETRY.getType();
    }

    @Override
    protected LocalDateTime getLastAt(final String groupName) {
        return LAST_AT_MAP.get(groupName);
    }

    @Override
    protected LocalDateTime putLastAt(final String groupName, final LocalDateTime LocalDateTime) {
        return LAST_AT_MAP.put(groupName, LocalDateTime);
    }

    private WaitStrategy getWaitWaitStrategy(String groupName, String sceneName) {

        SceneConfig sceneConfig = configAccess.getSceneConfigByGroupNameAndSceneName(groupName, sceneName);
        Integer backOff = sceneConfig.getBackOff();

        return WaitStrategies.WaitStrategyEnum.getWaitStrategy(backOff);
    }

    @Override
    protected ActorRef getActorRef() {
        return ActorGenerator.execUnitActor();
    }


}
