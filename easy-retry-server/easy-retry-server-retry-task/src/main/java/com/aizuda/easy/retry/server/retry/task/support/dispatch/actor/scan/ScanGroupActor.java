package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor.scan;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.retry.task.support.RetryContext;
import com.aizuda.easy.retry.server.retry.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.retry.task.support.context.MaxAttemptsPersistenceRetryContext;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryBuilder;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.server.retry.task.support.strategy.FilterStrategies;
import com.aizuda.easy.retry.server.retry.task.support.strategy.StopStrategies;
import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
public class ScanGroupActor extends AbstractScanGroup {

    public static final String BEAN_NAME = "ScanGroupActor";
    /**
     * 缓存待拉取数据的起点id
     * <p>
     * LAST_AT_MAP[key] = groupName  LAST_AT_MAP[value] = retry_task的id
     */
    private static final ConcurrentMap<String, Long> LAST_AT_MAP = new ConcurrentHashMap<>();

    @Override
    protected RetryContext<Result<DispatchRetryResultDTO>> builderRetryContext(final String groupName,
        final RetryTask retryTask) {
        MaxAttemptsPersistenceRetryContext<Result<DispatchRetryResultDTO>> retryContext = new MaxAttemptsPersistenceRetryContext<>();
        retryContext.setRetryTask(retryTask);
        retryContext.setSceneBlacklist(accessTemplate.getSceneConfigAccess().getBlacklist(groupName));
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
            .withFilterStrategy(FilterStrategies.triggerAtFilter())
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
    protected Long getLastId(final String groupName) {
        return LAST_AT_MAP.get(groupName);
    }

    @Override
    protected void putLastId(final String groupName, final Long lastId) {
        LAST_AT_MAP.put(groupName, lastId);
    }


    private WaitStrategy getWaitWaitStrategy(String groupName, String sceneName) {

        SceneConfig sceneConfig = accessTemplate.getSceneConfigAccess().getSceneConfigByGroupNameAndSceneName(groupName, sceneName);
        Integer backOff = sceneConfig.getBackOff();

        return WaitStrategies.WaitStrategyEnum.getWaitStrategy(backOff);
    }

    @Override
    protected ActorRef getActorRef() {
        return ActorGenerator.execUnitActor();
    }


}
