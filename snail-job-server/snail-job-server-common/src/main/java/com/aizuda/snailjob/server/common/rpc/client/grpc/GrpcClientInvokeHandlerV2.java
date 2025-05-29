package com.aizuda.snailjob.server.common.rpc.client.grpc;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheToken;
import com.aizuda.snailjob.server.common.dto.GrpcClientInvokeConfig;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.InstanceSelectCondition;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.server.common.rpc.client.SnailJobRetryListener;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Header;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Param;
import com.github.rholder.retry.*;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求处理器
 *
 * @author: opensnail
 * @date : 2023-05-11 21:45
 * @since 2.0.0
 */
@Slf4j
public class GrpcClientInvokeHandlerV2 implements InvocationHandler {

    public static final String NEW_INSTANCE_LIVE_INFO = "newInstanceLiveInfo";
    public static final AtomicLong REQUEST_ID = new AtomicLong(0);

    private InstanceLiveInfo instanceLiveInfo;
    private final boolean failRetry;
    private final int retryTimes;
    private final int retryInterval;
    private final SnailJobRetryListener retryListener;
    private final boolean failover;
    private final Integer routeKey;
    private final String allocKey;
    private final Integer executorTimeout;
    private final boolean async;
    private final String groupName;
    private final String namespaceId;

    public GrpcClientInvokeHandlerV2(GrpcClientInvokeConfig config) {
        this.instanceLiveInfo = config.getInstanceLiveInfo();
        RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
        this.groupName = nodeInfo.getGroupName();
        this.namespaceId = nodeInfo.getNamespaceId();
        this.failRetry = config.isFailRetry();
        this.retryTimes = config.getRetryTimes();
        this.retryInterval = config.getRetryInterval();
        this.retryListener = config.getRetryListener();
        this.routeKey = config.getRouteKey();
        this.allocKey = config.getAllocKey();
        this.failover = config.isFailover();
        this.executorTimeout = config.getExecutorTimeout();
        this.async = config.isAsync();
    }

    @Override
    public Result invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Mapping annotation = method.getAnnotation(Mapping.class);
        Assert.notNull(annotation, () -> new SnailJobServerException("@Mapping cannot be null"));
        return requestRemote(method, args, annotation);
    }

    private Result requestRemote(Method method, Object[] args, Mapping mapping) throws Throwable {

        try {
            // 参数解析
            ParseParasResult parasResult = doParseParams(method, args);

            // 若是POST请求，请求体不能是null
            if (RequestMethod.POST.name().equals(mapping.method().name())) {
                Assert.notNull(parasResult.body, () -> new SnailJobServerException("body cannot be null"));
            }

            Retryer<Result> retryer = buildResultRetryer();

            Map<String, String> requestHeaders = parasResult.requestHeaders;
            // 统一设置Token
            requestHeaders.put(SystemConstants.SNAIL_JOB_AUTH_TOKEN, CacheToken.get(groupName, namespaceId));

            long reqId = newId();
            Result result = retryer.call(() -> {
                RegisterNodeInfo nodeInfo = instanceLiveInfo.getNodeInfo();
                StopWatch sw = new StopWatch();

                sw.start("request start " + reqId);

                ListenableFuture<GrpcResult> future;
                try {
                    ManagedChannel channel = instanceLiveInfo.getChannel();
                    future = GrpcChannel.send(mapping.path(), JsonUtil.toJsonString(args), requestHeaders, reqId, channel);
                } finally {
                    sw.stop();
                }

                SnailJobLog.LOCAL.debug("Request complete requestId:[{}] took [{}ms]", reqId, sw.getTotalTimeMillis());

                if (async) {
                    // 暂时不支持异步调用
                    return null;
                } else {
                    Assert.notNull(future, () -> new SnailJobServerException("completableFuture is null"));
                    try {
                        GrpcResult grpcResult = future.get(Optional.ofNullable(executorTimeout).orElse(20),
                                TimeUnit.SECONDS);
                        Object obj = JsonUtil.parseObject(grpcResult.getData(), Object.class);
                        return new Result(grpcResult.getStatus(), grpcResult.getMessage(), obj);
                    } catch (Exception ex) {
                        log.error("request client I/O error, clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]",
                                nodeInfo.getHostId(), nodeInfo.getHostIp(), nodeInfo.getHostIp(),
                                NetUtil.getLocalIpStr(), ex);
                        InstanceManager instanceManager = SnailSpringContext.getBean(InstanceManager.class);
                        instanceLiveInfo.setAlive(Boolean.FALSE);

                        // 故障转移
                        failoverHandler(nodeInfo, instanceManager);

                        throw ex;
                    }
                }
            });

            return result;
        } catch (ExecutionException ex) {
            throw ex.getCause();
        } catch (Exception ex) {
            Throwable throwable = ex;
            if (ex.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) ex;
                throwable = re.getLastFailedAttempt().getExceptionCause();
            }

            throw throwable;
        }
    }

    private void failoverHandler(RegisterNodeInfo nodeInfo, InstanceManager instanceManager) {
        if (!failover) {
            return;
        }

        InstanceSelectCondition condition = InstanceSelectCondition
                .builder()
                .allocKey(allocKey)
                .routeKey(routeKey)
                .namespaceId(nodeInfo.getNamespaceId())
                .groupName(nodeInfo.getGroupName())
                .targetLabels(JsonUtil.toJsonString(nodeInfo.getLabelMap()))
                .build();

        this.instanceLiveInfo = instanceManager.getALiveInstanceByRouteKey(condition);
        if (instanceLiveInfo == null) {
            return;
        }

        if (Objects.nonNull(retryListener)) {
            Map<String, Object> properties = retryListener.properties();
            properties.put(NEW_INSTANCE_LIVE_INFO, instanceLiveInfo);
        }

        RegisterNodeInfo newNodeInfo = instanceLiveInfo.getNodeInfo();
        log.error("request client I/O error, choose new node clientId:[{}] clientAddr:[{}] serverIp:[{}]",
                newNodeInfo.getHostId(), newNodeInfo.address(), NetUtil.getLocalIpStr());

    }

    private Retryer<Result> buildResultRetryer() {
        Retryer<Result> retryer = RetryerBuilder.<Result>newBuilder()
                .retryIfException(throwable -> failRetry)
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryTimes <= 0 ? 1 : retryTimes))
                .withWaitStrategy(WaitStrategies.fixedWait(Math.max(retryInterval, 0), TimeUnit.SECONDS))
                .withRetryListener(retryListener)
                .build();
        return retryer;
    }

    private ParseParasResult doParseParams(Method method, Object[] args) {

        Object body = null;
        Map<String, String> requestHeaders = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        // 解析参数
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(Body.class)) {
                body = args[i];
            } else if ((parameter.isAnnotationPresent(Header.class))) {
                requestHeaders.put(SystemConstants.SNAIL_JOB_HEAD_KEY, JsonUtil.toJsonString(args[i]));
            } else if ((parameter.isAnnotationPresent(Param.class))) {
                paramMap.put(parameter.getAnnotation(Param.class).name(), args[i]);
            } else {
                throw new SnailJobServerException("parameter error");
            }
        }

        ParseParasResult parseParasResult = new ParseParasResult();
        parseParasResult.setBody(body);
        parseParasResult.setParamMap(paramMap);
        parseParasResult.setRequestHeaders(requestHeaders);
        return parseParasResult;
    }

    @Data
    private static class ParseParasResult {

        private Object body = null;
        private Map<String, String> requestHeaders;
        private Map<String, Object> paramMap;
    }

    private static long newId() {
        return REQUEST_ID.getAndIncrement();
    }
}
