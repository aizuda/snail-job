package com.aizuda.snailjob.client.common.rpc.client;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.exception.SnailJobClientTimeOutException;
import com.aizuda.snailjob.client.common.rpc.client.netty.NettyChannel;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.rpc.RpcContext;
import com.aizuda.snailjob.common.core.rpc.SnailJobFuture;
import com.aizuda.snailjob.common.log.SnailJobLog;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * 请求处理器
 *
 * @author: opensnail
 * @date : 2023-05-11 21:45
 * @since 1.3.0
 */
public class RpcClientInvokeHandler<R extends Result<Object>> implements InvocationHandler {

    private final Consumer<R> consumer;
    private final boolean async;
    private final long timeout;
    private final TimeUnit unit;

    public RpcClientInvokeHandler(boolean async, long timeout, TimeUnit unit, Consumer<R> consumer) {
        this.consumer = consumer;
        this.async = async;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public R invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);
        SnailJobRequest snailJobRequest = new SnailJobRequest(args);

        sw.start("request start " + snailJobRequest.getReqId());

        SnailJobFuture<R> newFuture = SnailJobFuture.newFuture(snailJobRequest.getReqId(),
                timeout,
                unit);
        RpcContext.setFuture(newFuture);

        try {
            NettyChannel.send(HttpMethod.valueOf(annotation.method().name()), annotation.path(), snailJobRequest.toString());
        } finally {
            sw.stop();
        }

        SnailJobLog.LOCAL.debug("request complete requestId:[{}] 耗时:[{}ms]", snailJobRequest.getReqId(), sw.getTotalTimeMillis());
        if (async) {
            newFuture.whenComplete((r, t) -> {
                if (Objects.nonNull(t)) {
                    consumer.accept(
                            (R) new SnailJobRpcResult(StatusEnum.NO.getStatus(), t.getMessage(), null, snailJobRequest.getReqId()));
                } else {
                    consumer.accept(r);
                }
            });
            return null;
        } else {
            Assert.notNull(newFuture, () -> new SnailJobClientException("completableFuture is null"));
            try {
                return newFuture.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException e) {
                throw new SnailJobClientTimeOutException("Request to remote interface timed out. path:[{}]", annotation.path());
            }
        }

    }

}
