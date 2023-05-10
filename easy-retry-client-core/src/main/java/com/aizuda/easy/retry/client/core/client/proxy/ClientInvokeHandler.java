package com.aizuda.easy.retry.client.core.client.proxy;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.client.core.annotation.Mapping;
import com.aizuda.easy.retry.client.core.client.netty.NettyHttpConnectClient;
import com.aizuda.easy.retry.client.core.client.netty.RpcContext;
import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

    private Consumer<R> consumer;
    private boolean async;
    private long timeout;
    private TimeUnit unit;

    public ClientInvokeHandler(boolean async, long timeout, TimeUnit unit, Consumer<R> consumer) {
        this.consumer = consumer;
        this.async = async;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public R invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);
        EasyRetryRequest easyRetryRequest = new EasyRetryRequest(args);

        sw.start("request start " + easyRetryRequest.getRequestId());

        CompletableFuture<R> completableFuture = null;
        if (async) {
            RpcContext.setCompletableFuture(easyRetryRequest.getRequestId(), consumer);
        } else {
            completableFuture = new CompletableFuture<>();
            RpcContext.setCompletableFuture(easyRetryRequest.getRequestId(), completableFuture);
        }

        try {
            NettyHttpConnectClient.send(HttpMethod.valueOf(annotation.method().name()), annotation.path(),
                JsonUtil.toJsonString(easyRetryRequest));
        } catch (Exception e) {
            throw e;
        } finally {
            sw.stop();
        }

        LogUtils.info(log,"request complete requestId:[{}] 耗时:[{}ms]", easyRetryRequest.getRequestId(), sw.getTotalTimeMillis());
        if (async) {
            return null;
        } else {
            Assert.notNull(completableFuture, () -> new EasyRetryClientException("completableFuture is null"));
            return completableFuture.get(timeout, unit);
        }

    }

}
