package com.aizuda.easy.retry.server.retry.task.support.dispatch.task;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.retry.task.support.RetryContext;
import com.aizuda.easy.retry.server.retry.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.retry.task.support.context.CallbackRetryContext;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryBuilder;
import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.easy.retry.server.retry.task.support.strategy.FilterStrategies;
import com.aizuda.easy.retry.server.retry.task.support.strategy.StopStrategies;
import com.aizuda.easy.retry.server.retry.task.support.strategy.WaitStrategies;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.SceneConfig;
import org.springframework.stereotype.Component;

/**
 * 回调任务执行器
 *
 * @author www.byteblogs.com
 * @date 2023-09-23 08:03:07
 * @since 2.4.0
 */
@Component
public class CallbackTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected RetryContext builderRetryContext(final String groupName, final RetryTask retryTask,
        final SceneConfig sceneConfig) {

        CallbackRetryContext<Result> retryContext = new CallbackRetryContext<>();
        retryContext.setRetryTask(retryTask);
        retryContext.setSceneBlacklist(accessTemplate.getSceneConfigAccess().getBlacklist(groupName));
        retryContext.setServerNode(
            clientNodeAllocateHandler.getServerNode(retryTask.getSceneName(), retryTask.getGroupName(),
                sceneConfig.getRouteKey()));
        retryContext.setSceneConfig(sceneConfig);
        return retryContext;
    }

    @Override
    protected RetryExecutor builderResultRetryExecutor(RetryContext retryContext, final SceneConfig sceneConfig) {
        return RetryBuilder.<Result>newBuilder()
                .withStopStrategy(StopStrategies.stopException())
                .withStopStrategy(StopStrategies.stopResultStatus())
                .withWaitStrategy(getWaitWaitStrategy())
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
    public TaskExecutorSceneEnum getTaskType() {
        return TaskExecutorSceneEnum.AUTO_CALLBACK;
    }

    private WaitStrategy getWaitWaitStrategy() {
        // 回调失败每15min重试一次
        return WaitStrategies.WaitStrategyEnum.getWaitStrategy(WaitStrategies.WaitStrategyEnum.FIXED.getBackOff());
    }

    @Override
    protected ActorRef getActorRef() {
        return ActorGenerator.execCallbackUnitActor();
    }

}
