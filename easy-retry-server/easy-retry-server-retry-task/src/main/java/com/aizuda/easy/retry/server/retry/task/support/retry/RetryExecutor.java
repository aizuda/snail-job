package com.aizuda.easy.retry.server.retry.task.support.retry;

import akka.actor.ActorRef;
import cn.hutool.core.lang.Pair;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.retry.task.dto.RetryLogMetaDTO;
import com.aizuda.easy.retry.server.retry.task.support.FilterStrategy;
import com.aizuda.easy.retry.server.retry.task.support.RetryContext;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.server.retry.task.support.StopStrategy;
import com.aizuda.easy.retry.server.common.WaitStrategy;
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

    private final List<StopStrategy> stopStrategies;
    private final WaitStrategy waitStrategy;
    private final List<FilterStrategy> filterStrategies;
    private final RetryContext<V> retryContext;

    public RetryExecutor(List<StopStrategy> stopStrategies,
                         WaitStrategy waitStrategy,
                         List<FilterStrategy> filterStrategies,
                         RetryContext<V> retryContext) {
        this.stopStrategies = stopStrategies;
        this.waitStrategy = waitStrategy;
        this.filterStrategies = filterStrategies;
        this.retryContext = retryContext;
    }

    public Pair<Boolean /*是否符合条件*/, StringBuilder/*描述信息*/> filter() {

        for (FilterStrategy filterStrategy : filterStrategies) {
            Pair<Boolean, StringBuilder> pair = filterStrategy.filter(retryContext);
            if (!pair.getKey()) {
                return pair;
            }
        }

        return Pair.of(Boolean.TRUE, new StringBuilder());
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
            retryContext.setCallResult(call);
        } catch (Exception e) {
            RetryLogMetaDTO retryLogMetaDTO = RetryTaskConverter.INSTANCE.toLogMetaDTO(retryContext.getRetryTask());
            retryLogMetaDTO.setTimestamp(DateUtils.toNowMilli());
            EasyRetryLog.REMOTE.error("请求客户端执行失败. uniqueId:[{}] <|>{}<|>", retryContext.getRetryTask().getUniqueId(), e);
            retryContext.setException(e);
        }

        boolean isStop = Boolean.TRUE;

        // 触发停止策略判断
        for (StopStrategy stopStrategy : stopStrategies) {
            if (stopStrategy.supports(retryContext)) {
                // 必须责任链中的所有停止策略都判断为停止，此时才判定为重试完成
                if (!stopStrategy.shouldStop(retryContext)) {
                    isStop = Boolean.FALSE;
                }
            }
        }

        ActorRef actorRef;
        if (isStop) {
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
