package com.aizuda.easy.retry.server.support.dispatch.actor.scan;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.enums.TaskTypeEnum;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTask;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.WaitStrategy;
import com.aizuda.easy.retry.server.support.context.CallbackRetryContext;
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
 * @since 1.5.0
 */
@Component(ScanCallbackGroupActor.BEAN_NAME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class ScanCallbackGroupActor extends AbstractScanGroup {

    public static final String BEAN_NAME = "ScanCallbackGroupActor";

    /**
     * 缓存待拉取数据的起点id
     * <p>
     * LAST_AT_MAP[key] = groupName  LAST_AT_MAP[value] = retry_task的 id
     */
    private static final ConcurrentMap<String, Long> LAST_AT_MAP = new ConcurrentHashMap<>();

    @Override
    protected RetryContext builderRetryContext(final String groupName, final RetryTask retryTask) {

        CallbackRetryContext<Result> retryContext = new CallbackRetryContext<>();
        retryContext.setRetryTask(retryTask);
        retryContext.setSceneBlacklist(configAccess.getBlacklist(groupName));
        retryContext.setServerNode(clientNodeAllocateHandler.getServerNode(retryTask.getGroupName()));
        return retryContext;
    }

    @Override
    protected RetryExecutor builderResultRetryExecutor(RetryContext retryContext) {
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
    protected Integer getTaskType() {
        return TaskTypeEnum.CALLBACK.getType();
    }

    @Override
    protected Long getLastId(final String groupName) {
        return LAST_AT_MAP.get(groupName);
    }

    @Override
    protected void putLastId(final String groupName, final Long lastId) {
        LAST_AT_MAP.put(groupName, lastId);
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
