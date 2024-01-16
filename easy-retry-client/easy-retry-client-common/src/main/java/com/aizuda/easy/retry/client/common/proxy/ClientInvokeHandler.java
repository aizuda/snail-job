package com.aizuda.easy.retry.client.common.proxy;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.common.annotation.Mapping;
import com.aizuda.easy.retry.client.common.exception.EasyRetryClientException;
import com.aizuda.easy.retry.client.common.exception.EasyRetryClientTimeOutException;
import com.aizuda.easy.retry.client.common.netty.NettyChannel;
import com.aizuda.easy.retry.client.common.netty.RpcContext;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
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
 * @author: www.byteblogs.com
 * @date : 2023-05-11 21:45
 * @since 1.3.0
 */
@Slf4j
public class ClientInvokeHandler<R> implements InvocationHandler {

    private final Consumer<R> consumer;
    private final boolean async;
    private final long timeout;
    private final TimeUnit unit;

    public ClientInvokeHandler(boolean async, long timeout, TimeUnit unit, Consumer<R> consumer) {
        this.consumer = consumer;
        this.async = async;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public R invoke(final Object proxy, final Method method, final Object[] args) throws InterruptedException {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);
        EasyRetryRequest easyRetryRequest = new EasyRetryRequest(args);

        sw.start("request start " + easyRetryRequest.getReqId());

        CompletableFuture<R> completableFuture = null;
        if (async) {
            RpcContext.setCompletableFuture(easyRetryRequest.getReqId(), consumer);
        } else {
            completableFuture = new CompletableFuture<>();
            RpcContext.setCompletableFuture(easyRetryRequest.getReqId(), completableFuture);
        }

        try {
            NettyChannel.send(HttpMethod.valueOf(annotation.method().name()), annotation.path(), easyRetryRequest.toString());
        } finally {
            sw.stop();
        }

       EasyRetryLog.LOCAL.info("request complete requestId:[{}] 耗时:[{}ms]", easyRetryRequest.getReqId(), sw.getTotalTimeMillis());
        if (async) {
            return null;
        } else {
            Assert.notNull(completableFuture, () -> new EasyRetryClientException("completableFuture is null"));
            try {
                return completableFuture.get(timeout, unit);
            } catch (ExecutionException e) {
                throw new EasyRetryClientException("Request to remote interface exception. path:[{}]",  annotation.path());
            } catch (TimeoutException e) {
                throw new EasyRetryClientTimeOutException("Request to remote interface timed out. path:[{}]", annotation.path());
            }
        }

    }

}
