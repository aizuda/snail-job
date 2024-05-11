package com.aizuda.snailjob.common.core.rpc;

import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.exception.SnailJobRemotingTimeOutException;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 处理RPC超时和回调
 *
 * @author: opensnail
 * @date : 2023-05-12 09:05
 * @since 1.3.0
 */
@Slf4j
public final class RpcContext {

    private RpcContext() {
    }

    private static final HashedWheelTimer WHEEL_TIMER;

    static {
        WHEEL_TIMER = new HashedWheelTimer(
            new CustomizableThreadFactory("snail-job-rpc-timeout-"), 1,
            TimeUnit.SECONDS, 1024);
    }

    private static final ConcurrentMap<Long, SnailJobFuture> COMPLETABLE_FUTURE = new ConcurrentHashMap<>();

    public static void invoke(Long requestId, NettyResult nettyResult, boolean timeout) {

        try {
            // 同步请求同步返回
            Optional.ofNullable(COMPLETABLE_FUTURE.remove(requestId))
                .ifPresent(future -> {
                    if (timeout) {
                        future.completeExceptionally(new SnailJobRemotingTimeOutException("Request to remote interface timed out."));
                    } else {
                        future.complete(nettyResult);
                    }
                });

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("回调处理失败 requestId:[{}]", requestId, e);
        }
    }

    public static <R extends Result<Object>> void setFuture(SnailJobFuture<R> future) {
        if (Objects.nonNull(future)) {
            COMPLETABLE_FUTURE.put(future.getRequestId(), future);
        }

        // 放入时间轮
        WHEEL_TIMER.newTimeout(new TimeoutCheckTask(future.getRequestId()), future.getTimeout(), future.getUnit());
    }

    public static class TimeoutCheckTask implements TimerTask {

        private final Long requestId;
        public TimeoutCheckTask(Long requestId) {
            this.requestId = requestId;
        }
        @Override
        public void run(final Timeout timeout) throws Exception {
            invoke(requestId, new NettyResult(StatusEnum.NO.getStatus(), "Request to remote interface timed out.", null, requestId), true);
        }
    }

}
