package com.aizuda.snailjob.client.common.rpc.client;

import cn.hutool.core.date.StopWatch;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.exception.SnailJobClientTimeOutException;
import com.aizuda.snailjob.common.core.enums.HeadersEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcSnailJobRequest;
import com.aizuda.snailjob.common.core.grpc.auto.Metadata;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import com.google.protobuf.UnsafeByteOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
public class GrpcClientInvokeHandler<R extends Result<Object>> implements InvocationHandler {
    private final Consumer<R> consumer;
    private final boolean async;
    private final long timeout;
    private final TimeUnit unit;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final  ScheduledThreadPoolExecutor schedule =  new ScheduledThreadPoolExecutor(1);
    public GrpcClientInvokeHandler(boolean async, long timeout, TimeUnit unit, Consumer<R> consumer) {
        this.consumer = consumer;
        this.async = async;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public R invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        StopWatch sw = new StopWatch();
        Mapping annotation = method.getAnnotation(Mapping.class);

        ListenableFuture<GrpcResult> future = GrpcChannel.sendOfUnary(annotation.path(), JsonUtil.toJsonString(args));
        SnailJobLog.LOCAL.debug("request complete requestId:[{}] 耗时:[{}ms]", sw.getTotalTimeMillis());

        if (async) {
            if (future == null) {
                return null;
            }

            Futures.addCallback(future, new FutureCallback<>() {

                @Override
                public void onSuccess(final GrpcResult result) {

                    ByteBuffer byteBuffer = result.getData().getValue().asReadOnlyByteBuffer();
                    String str = JsonUtil.parseObject(new ByteBufferBackedInputStream(byteBuffer), String.class);
                    consumer.accept((R) new NettyResult(result.getStatus(), result.getMessage(), str, 0));
                }

                @Override
                public void onFailure(final Throwable t) {
                    consumer.accept((R) new NettyResult(StatusEnum.NO.getStatus(), t.getMessage(), null, 1));
                }
            }, executorService);

            Futures.withTimeout(future, timeout, unit, schedule);
            return null;
        } else {

            try {
                GrpcResult result = future.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
                ByteBuffer byteBuffer = result.getData().getValue().asReadOnlyByteBuffer();
                String str = JsonUtil.parseObject(new ByteBufferBackedInputStream(byteBuffer), String.class);
                return (R) new Result(result.getStatus(), result.getMessage(), str);
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException e) {
                throw new SnailJobClientTimeOutException("Request to remote interface timed out. path:[{}]", annotation.path());
            }
        }

    }

}
