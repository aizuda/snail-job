package com.aizuda.easy.retry.client.core.client.netty;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.EasyRetryRequest;
import com.aizuda.easy.retry.common.core.model.NettyResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * @author: www.byteblogs.com
 * @date : 2023-05-12 09:05
 * @since 1.3.0
 */
@Slf4j
public class RpcContext {

    private static final ConcurrentMap<String, CompletableFuture> COMPLETABLE_FUTURE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Consumer> CALLBACK_CONSUMER = new ConcurrentHashMap<>();

    public static void invoke(String requestId, NettyResult nettyResult) {

        try {
            // 同步请求同步返回
            Optional.ofNullable(getCompletableFuture(requestId)).ifPresent(completableFuture -> completableFuture.complete(nettyResult));

            // 异步请求进行回调
            Optional.ofNullable(getConsumer(requestId)).ifPresent(consumer -> consumer.accept(nettyResult));
        } catch (Exception e) {
            LogUtils.error(log, "回调处理失败 requestId:[{}]",requestId, e );
        } finally {
            COMPLETABLE_FUTURE.remove(requestId);
            CALLBACK_CONSUMER.remove(requestId);
        }

    }

    public static <R> void setCompletableFuture(String id, CompletableFuture<R> completableFuture, Consumer<R> callable) {
        if (Objects.nonNull(completableFuture)) {
            COMPLETABLE_FUTURE.put(id, completableFuture);
        }

        if (Objects.nonNull(callable)) {
            CALLBACK_CONSUMER.put(id, callable);
        }

    }

    public static <R> void setCompletableFuture(String id, Consumer<R> callable) {
        setCompletableFuture(id, null, callable);
    }

    public static <R> void setCompletableFuture(String id, CompletableFuture<R> completableFuture) {
        setCompletableFuture(id, completableFuture, null);
    }

    public static CompletableFuture<Object> getCompletableFuture(String id) {
        return COMPLETABLE_FUTURE.get(id);
    }

    public static Consumer getConsumer(String id) {
        return CALLBACK_CONSUMER.get(id);
    }
}
