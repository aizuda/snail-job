package com.aizuda.snailjob.client.common.rpc.client;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.exception.SnailJobClientTimeOutException;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
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
public class RpcClientInvokeHandler<R> implements InvocationHandler {

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
    public R invoke(final Object proxy, final Method method, final Object[] args) throws InterruptedException {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);
        SnailJobRequest snailJobRequest = new SnailJobRequest(args);

        sw.start("request start " + snailJobRequest.getReqId());

        CompletableFuture<R> completableFuture = null;
        if (async) {
            RpcContext.setCompletableFuture(snailJobRequest.getReqId(), consumer);
        } else {
            completableFuture = new CompletableFuture<>();
            RpcContext.setCompletableFuture(snailJobRequest.getReqId(), completableFuture);
        }

        try {
            NettyChannel.send(HttpMethod.valueOf(annotation.method().name()), annotation.path(), snailJobRequest.toString());
        } finally {
            sw.stop();
        }

       SnailJobLog.LOCAL.debug("request complete requestId:[{}] 耗时:[{}ms]", snailJobRequest.getReqId(), sw.getTotalTimeMillis());
        if (async) {
            return null;
        } else {
            Assert.notNull(completableFuture, () -> new SnailJobClientException("completableFuture is null"));
            try {
                return completableFuture.get(timeout, unit);
            } catch (ExecutionException e) {
                throw new SnailJobClientException("Request to remote interface exception. path:[{}]",  annotation.path());
            } catch (TimeoutException e) {
                throw new SnailJobClientTimeOutException("Request to remote interface timed out. path:[{}]", annotation.path());
            } finally {
                RpcContext.remove(snailJobRequest.getReqId());
            }
        }

    }

}
