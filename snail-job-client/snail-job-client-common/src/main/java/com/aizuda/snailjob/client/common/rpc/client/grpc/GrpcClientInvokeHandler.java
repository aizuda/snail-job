package com.aizuda.snailjob.client.common.rpc.client.grpc;

import cn.hutool.core.date.StopWatch;
import com.aizuda.snailjob.client.common.annotation.Header;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.RpcClientProperties;
import com.aizuda.snailjob.client.common.config.SnailJobProperties.ThreadPoolConfig;
import com.aizuda.snailjob.client.common.exception.SnailJobClientTimeOutException;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 请求处理器
 *
 * @author: opensnail
 * @date : 2023-05-11 21:45
 * @since 1.3.0
 */
public class GrpcClientInvokeHandler<R extends Result<Object>> implements InvocationHandler {

    public static final AtomicLong REQUEST_ID = new AtomicLong(0);
    private final Consumer<R> consumer;
    private final boolean async;
    private final long timeout;
    private final TimeUnit unit;

    private static final ExecutorService executorService = createGrpcExecutor();

    private static final ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(1);

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

        long reqId = newId();
        ListenableFuture<GrpcResult> future = GrpcChannel.sendOfUnary(annotation.path(), JsonUtil.toJsonString(args),
            reqId, getHeaderInfo(method, args));
        SnailJobLog.LOCAL.debug("Request complete requestId:[{}] took [{}ms]", sw.getTotalTimeMillis(), reqId);
        if (future == null) {
            return (R) new SnailJobRpcResult(StatusEnum.NO.getStatus(), "future is nulll", null, reqId);
        }

        if (async) {
            Futures.addCallback(future, new FutureCallback<>() {

                @Override
                public void onSuccess(final GrpcResult result) {

                    Object obj = JsonUtil.parseObject( result.getData(), Object.class);
                    consumer.accept(
                            (R) new SnailJobRpcResult(result.getStatus(), result.getMessage(), obj, result.getReqId()));
                }

                @Override
                public void onFailure(final Throwable t) {
                    consumer.accept((R) new SnailJobRpcResult(StatusEnum.NO.getStatus(), t.getMessage(), null, reqId));
                }
            }, executorService);

            Futures.withTimeout(future, timeout, unit, schedule);
            return null;
        } else {

            try {
                GrpcResult result = future.get(timeout, unit);
                Object obj = JsonUtil.parseObject(result.getData(), Object.class);
                return (R) new SnailJobRpcResult(result.getStatus(), result.getMessage(), obj, result.getReqId());
            } catch (ExecutionException e) {
                throw e.getCause();
            } catch (TimeoutException e) {
                throw new SnailJobClientTimeOutException("Request to remote interface timed out. path:[{}]",
                    annotation.path());
            }
        }

    }

    private static long newId() {
        return REQUEST_ID.getAndIncrement();
    }

    private Map<String, String> getHeaderInfo(Method method, Object[] args) {
        Map<String, String> map = new HashMap<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Header.class)) {
                Header header = parameter.getAnnotation(Header.class);
                Object o = args[i];
                if (Objects.nonNull(o)) {
                    map.put(header.name().getKey(), JsonUtil.toJsonString(o));
                }
            }
        }

        return map;
    }

    protected static ThreadPoolExecutor createGrpcExecutor() {
        SnailJobProperties snailJobProperties = SnailSpringContext.getBean(SnailJobProperties.class);
        RpcClientProperties clientRpc = snailJobProperties.getClientRpc();
        String serverIp = GrpcChannel.getServerHost().replaceAll("%", "-");
        ThreadPoolConfig threadPool = clientRpc.getClientTp();
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
            threadPool.getMaximumPoolSize(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(threadPool.getQueueCapacity()),
            new ThreadFactoryBuilder().setDaemon(true)
                .setNameFormat("snail-job-grpc-client-executor-" + serverIp + "-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(true);
        return grpcExecutor;
    }
}
