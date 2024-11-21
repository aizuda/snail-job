package com.aizuda.snailjob.server.common.rpc.client;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.exception.SnailJobRemotingTimeOutException;
import com.aizuda.snailjob.common.core.grpc.auto.GrpcResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.rpc.RpcContext;
import com.aizuda.snailjob.common.core.rpc.SnailJobFuture;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.NetUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.cache.CacheToken;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Body;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Header;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Mapping;
import com.aizuda.snailjob.server.common.rpc.client.annotation.Param;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.util.concurrent.ListenableFuture;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
public class GrpcClientInvokeHandler implements InvocationHandler {

    public static final AtomicLong REQUEST_ID = new AtomicLong(0);

    private final String groupName;
    private String hostId;
    private String hostIp;
    private Integer hostPort;
    private final boolean failRetry;
    private final int retryTimes;
    private final int retryInterval;
    private final RetryListener retryListener;
    private final boolean failover;
    private final Integer routeKey;
    private final String allocKey;
    private final Integer executorTimeout;
    private final String namespaceId;
    private final boolean async;

    public GrpcClientInvokeHandler(final String groupName, final RegisterNodeInfo registerNodeInfo,
        final boolean failRetry, final int retryTimes,
        final int retryInterval, final RetryListener retryListener, final Integer routeKey, final String allocKey,
        final boolean failover, final Integer executorTimeout, final String namespaceId) {
        this.groupName = groupName;
        this.hostId = registerNodeInfo.getHostId();
        this.hostPort = registerNodeInfo.getHostPort();
        this.hostIp = registerNodeInfo.getHostIp();
        this.failRetry = failRetry;
        this.retryTimes = retryTimes;
        this.retryInterval = retryInterval;
        this.retryListener = retryListener;
        this.failover = failover;
        this.routeKey = routeKey;
        this.allocKey = allocKey;
        this.executorTimeout = executorTimeout;
        this.namespaceId = namespaceId;
        this.async = false;
    }

    @Override
    public Result invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Mapping annotation = method.getAnnotation(Mapping.class);
        Assert.notNull(annotation, () -> new SnailJobServerException("@Mapping cannot be null"));

        if (failover) {
            return doFailoverHandler(method, args, annotation);
        }

        return requestRemote(method, args, annotation, 1);
    }

    @NotNull
    private Result doFailoverHandler(final Method method, final Object[] args, final Mapping annotation)
        throws Throwable {
        Set<RegisterNodeInfo> serverNodeSet = CacheRegisterTable.getServerNodeSet(groupName, namespaceId);

        // 最多调用size次
        int size = serverNodeSet.size();
        for (int count = 1; count <= size; count++) {
            log.debug("Start request client. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count, hostId,
                hostIp, hostPort, NetUtil.getLocalIpStr());
            Result result = requestRemote(method, args, annotation, count);
            if (Objects.nonNull(result)) {
                return result;
            }
        }

        throw new SnailJobServerException("No available nodes.");
    }

    private Result requestRemote(Method method, Object[] args, Mapping mapping, int count) throws Throwable {

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

                StopWatch sw = new StopWatch();

                sw.start("request start " + reqId);

                ListenableFuture<GrpcResult> future;
                try {
                    future = GrpcChannel.send(hostId, hostIp, hostPort,
                        mapping.path(), JsonUtil.toJsonString(args), requestHeaders, reqId);
                } finally {
                    sw.stop();
                }

                SnailJobLog.LOCAL.debug("request complete requestId:[{}] 耗时:[{}ms]", reqId);

                if (async) {
                    // 暂时不支持异步调用
                    return null;
                } else {
                    Assert.notNull(future, () -> new SnailJobServerException("completableFuture is null"));
                    GrpcResult grpcResult = future.get(Optional.ofNullable(executorTimeout).orElse(20),
                        TimeUnit.SECONDS);
                    Object obj = JsonUtil.parseObject(grpcResult.getData(), Object.class);
                    return new Result(grpcResult.getStatus(), grpcResult.getMessage(), obj);
                }

            });

            log.debug("Request client success. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count,
                hostId,
                hostIp, hostPort, NetUtil.getLocalIpStr());

            return result;
        } catch (ExecutionException ex) {
            // 网络异常 TimeoutException |
            if (ex.getCause() instanceof SnailJobRemotingTimeOutException && failover) {
                log.error("request client I/O error, count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count,
                    hostId, hostIp, hostPort, NetUtil.getLocalIpStr(), ex);

                // 进行路由剔除处理
                CacheRegisterTable.remove(groupName, namespaceId, hostId);
                // 重新选一个可用的客户端节点
                ClientNodeAllocateHandler clientNodeAllocateHandler = SnailSpringContext.getBean(
                    ClientNodeAllocateHandler.class);
                RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(allocKey, groupName, namespaceId,
                    routeKey);
                // 这里表示无可用节点
                if (Objects.isNull(serverNode)) {
                    throw ex.getCause();
                }

                this.hostId = serverNode.getHostId();
                this.hostPort = serverNode.getHostPort();
                this.hostIp = serverNode.getHostIp();

            } else {
                // 其他异常继续抛出
                log.error("request client error.count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]", count,
                    hostId, hostIp, hostPort, NetUtil.getLocalIpStr(), ex);
                throw ex.getCause();
            }
        } catch (Exception ex) {
            log.error("request client unknown exception. count:[{}] clientId:[{}] clientAddr:[{}:{}] serverIp:[{}]",
                count, hostId, hostIp, hostPort, NetUtil.getLocalIpStr(), ex);

            Throwable throwable = ex;
            if (ex.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) ex;
                throwable = re.getLastFailedAttempt().getExceptionCause();
                if (throwable.getCause() instanceof SnailJobRemotingTimeOutException) {
                    // 若重试之后该接口仍然有问题，进行路由剔除处理
                    CacheRegisterTable.remove(groupName, namespaceId, hostId);
                }
            }

            throw throwable;
        }

        return null;
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
