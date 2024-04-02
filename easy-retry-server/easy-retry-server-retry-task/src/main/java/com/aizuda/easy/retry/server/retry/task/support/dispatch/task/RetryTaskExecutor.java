package com.aizuda.easy.retry.server.retry.task.support.dispatch.task;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.client.model.DispatchRetryResultDTO;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.retry.task.support.RetryContext;
import com.aizuda.easy.retry.server.common.WaitStrategy;
import com.aizuda.easy.retry.server.retry.task.support.context.MaxAttemptsPersistenceRetryContext;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryBuilder;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.server.retry.task.support.strategy.FilterStrategies;
import com.aizuda.easy.retry.server.retry.task.support.strategy.StopStrategies;
import com.aizuda.easy.retry.server.common.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import org.springframework.stereotype.Component;

/**
 * 重试任务执行器
 *
 * @author www.byteblogs.com
 * @date 2023-09-23 08:03:07
 * @since 2.4.0
 */
@Component
public class RetryTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected RetryContext<Result<DispatchRetryResultDTO>> builderRetryContext(final String groupName,
                                                                               final RetryTask retryTask,
                                                                               final SceneConfig sceneConfig) {
        MaxAttemptsPersistenceRetryContext<Result<DispatchRetryResultDTO>> retryContext = new MaxAttemptsPersistenceRetryContext<>();
        retryContext.setRetryTask(retryTask);
        retryContext.setSceneBlacklist(accessTemplate.getSceneConfigAccess().getBlacklist(groupName, sceneConfig.getNamespaceId()));
        retryContext.setServerNode(
                clientNodeAllocateHandler.getServerNode(retryTask.getSceneName(),
                        retryTask.getGroupName(),
                        retryTask.getNamespaceId(),
                        sceneConfig.getRouteKey()));
        retryContext.setSceneConfig(sceneConfig);
        return retryContext;
    }

    @Override
    protected RetryExecutor<Result<DispatchRetryResultDTO>> builderResultRetryExecutor(RetryContext retryContext,
                                                                                       final SceneConfig sceneConfig) {

        return RetryBuilder.<Result<DispatchRetryResultDTO>>newBuilder()
                .withStopStrategy(StopStrategies.stopException())
                .withStopStrategy(StopStrategies.stopResultStatusCode())
                .withWaitStrategy(getWaitWaitStrategy(sceneConfig))
                .withFilterStrategy(FilterStrategies.bitSetIdempotentFilter(idempotentStrategy))
                .withFilterStrategy(FilterStrategies.sceneBlackFilter())
                .withFilterStrategy(FilterStrategies.checkAliveClientPodFilter())
                .withFilterStrategy(FilterStrategies.rebalanceFilterStrategies())
                .withFilterStrategy(FilterStrategies.rateLimiterFilter())
                .withRetryContext(retryContext)
                .build();
    }

    @Override
    public TaskExecutorSceneEnum getTaskType() {
        return TaskExecutorSceneEnum.AUTO_RETRY;
    }

    private WaitStrategy getWaitWaitStrategy(SceneConfig sceneConfig) {
        Integer backOff = sceneConfig.getBackOff();
        return WaitStrategies.WaitStrategyEnum.getWaitStrategy(backOff);
    }

    @Override
    protected ActorRef getActorRef() {
        return ActorGenerator.execUnitActor();
    }

}
