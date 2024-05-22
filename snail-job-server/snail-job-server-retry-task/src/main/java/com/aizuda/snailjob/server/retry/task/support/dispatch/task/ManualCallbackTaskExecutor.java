package com.aizuda.snailjob.server.retry.task.support.dispatch.task;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.retry.task.support.RetryContext;
import com.aizuda.snailjob.server.retry.task.support.context.CallbackRetryContext;
import com.aizuda.snailjob.server.retry.task.support.retry.RetryBuilder;
import com.aizuda.snailjob.server.retry.task.support.retry.RetryExecutor;
import com.aizuda.snailjob.server.retry.task.support.strategy.FilterStrategies;
import com.aizuda.snailjob.server.retry.task.support.strategy.StopStrategies;
import com.aizuda.snailjob.template.datasource.persistence.po.RetrySceneConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import org.springframework.stereotype.Component;

/**
 * 回调任务执行器
 *
 * @author opensnail
 * @date 2023-09-23 08:03:07
 * @since 2.4.0
 */
@Component
public class ManualCallbackTaskExecutor extends AbstractTaskExecutor {

    @Override
    protected RetryContext builderRetryContext(final String groupName, final RetryTask retryTask,
                                               final RetrySceneConfig retrySceneConfig) {

        CallbackRetryContext<Result> retryContext = new CallbackRetryContext<>();
        retryContext.setRetryTask(retryTask);
        retryContext.setSceneBlacklist(accessTemplate.getSceneConfigAccess().getBlacklist(groupName, retrySceneConfig.getNamespaceId()));
        retryContext.setServerNode(
                clientNodeAllocateHandler.getServerNode(retryTask.getSceneName(),
                        retryTask.getGroupName(),
                        retryTask.getNamespaceId(),
                        retrySceneConfig.getRouteKey()));
        retryContext.setRetrySceneConfig(retrySceneConfig);
        return retryContext;
    }

    @Override
    protected RetryExecutor builderResultRetryExecutor(RetryContext retryContext, final RetrySceneConfig retrySceneConfig) {
        return RetryBuilder.<Result>newBuilder()
                .withStopStrategy(StopStrategies.stopException())
                .withStopStrategy(StopStrategies.stopResultStatus())
                .withWaitStrategy(getWaitWaitStrategy())
                .withFilterStrategy(FilterStrategies.bitSetIdempotentFilter(idempotentStrategy))
                .withFilterStrategy(FilterStrategies.sceneBlackFilter())
                .withFilterStrategy(FilterStrategies.checkAliveClientPodFilter())
                .withFilterStrategy(FilterStrategies.rebalanceFilterStrategies())
                .withFilterStrategy(FilterStrategies.rateLimiterFilter())
                .withRetryContext(retryContext)
                .build();
    }

    @Override
    protected boolean preCheck(RetryContext retryContext, RetryExecutor executor) {
        Pair<Boolean, StringBuilder> pair = executor.filter();
        Assert.isTrue(pair.getKey(), () -> new SnailJobServerException(pair.getValue().toString()));
        return pair.getKey();
    }

    @Override
    public TaskExecutorSceneEnum getTaskType() {
        return TaskExecutorSceneEnum.MANUAL_CALLBACK;
    }

    private WaitStrategy getWaitWaitStrategy() {
        // 回调失败每15min重试一次
        return WaitStrategies.WaitStrategyEnum.getWaitStrategy(WaitStrategies.WaitStrategyEnum.FIXED.getType());
    }

    @Override
    protected ActorRef getActorRef() {
        return ActorGenerator.execCallbackUnitActor();
    }

}
