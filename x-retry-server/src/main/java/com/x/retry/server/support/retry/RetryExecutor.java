package com.x.retry.server.support.retry;

import akka.actor.ActorRef;
import com.x.retry.server.akka.ActorGenerator;
import com.x.retry.server.support.FilterStrategy;
import com.x.retry.server.support.RetryContext;
import com.x.retry.server.support.StopStrategy;
import com.x.retry.server.support.WaitStrategy;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 重试执行器
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-29 18:57
 */
public class RetryExecutor<V> {

    private StopStrategy stopStrategy;
    private WaitStrategy waitStrategy;
    private List<FilterStrategy> filterStrategies;
    private RetryContext<V> retryContext;

    public RetryExecutor(StopStrategy stopStrategy,
                         WaitStrategy waitStrategy,
                         List<FilterStrategy> filterStrategies,
                         RetryContext retryContext) {
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

        // 调用重试
        V call = callable.call();

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
