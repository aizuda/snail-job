package com.aizuda.easy.retry.server.support.retry;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.akka.ActorGenerator;
import com.aizuda.easy.retry.server.support.FilterStrategy;
import com.aizuda.easy.retry.server.support.RetryContext;
import com.aizuda.easy.retry.server.support.StopStrategy;
import com.aizuda.easy.retry.server.support.WaitStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 重试执行器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:57
 */
@Slf4j
public class RetryExecutor<V> {

    private final StopStrategy stopStrategy;
    private final WaitStrategy waitStrategy;
    private final List<FilterStrategy> filterStrategies;
    private final RetryContext<V> retryContext;

    public RetryExecutor(StopStrategy stopStrategy,
                         WaitStrategy waitStrategy,
                         List<FilterStrategy> filterStrategies,
                         RetryContext<V> retryContext) {
        this.stopStrategy = stopStrategy;
        this.waitStrategy = waitStrategy;
        this.filterStrategies = filterStrategies;
        this.retryContext = retryContext;
    }

    public boolean filter() {

        for (FilterStrategy filterStrategy : filterStrategies) {
            if (!filterStrategy.filter(retryContext)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 重试执行
     *
     * @param callable 重试执行回调
     * @return 重试结果
     * @throws Exception
     */
    public V call(Callable<V> callable) throws Exception {

        // 这里调用客户端可能会出现网络异常
        V call = null;
        try {
            call = callable.call();
        } catch (Exception e) {
            log.error("客户端执行失败: [{}]", retryContext.getRetryTask());
        }

        retryContext.setCallResult(call);

        // 计算下次触发时间
        retryContext.getRetryTask().setNextTriggerAt(waitStrategy.computeRetryTime(retryContext));

        ActorRef actorRef;
        if (stopStrategy.shouldStop(retryContext)) {
            // 状态变为完成 FinishActor
            actorRef = ActorGenerator.finishActor();
        } else {
            // 失败 FailureActor
            actorRef = ActorGenerator.failureActor();
        }

        actorRef.tell(retryContext.getRetryTask(), actorRef);
        return call;
    }

    public RetryContext<V> getRetryContext() {
        return retryContext;
    }
}
