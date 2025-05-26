package com.aizuda.snailjob.server.common.rpc.client;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.GrpcClientInvokeConfig;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.rpc.client.grpc.GrpcClientInvokeHandler;
import com.aizuda.snailjob.server.common.rpc.client.grpc.GrpcClientInvokeHandlerV2;
import com.github.rholder.retry.RetryListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 构建请求类型
 *
 * @author: opensnail
 * @date : 2023-06-19 16:47
 * @since 2.0.0
 */
public class RequestBuilder<T, R> {

    private Class<T> clintInterface;
    private RegisterNodeInfo nodeInfo;
    private InstanceLiveInfo instanceLiveInfo;
    private boolean failRetry;
    private int retryTimes = 3;
    private int retryInterval = 1;
    private SnailJobRetryListener retryListener = new SimpleRetryListener();
    private boolean failover;
    private int routeKey;
    private String allocKey;
    private Integer executorTimeout;

    public static <T, R> RequestBuilder<T, R> newBuilder() {
        return new RequestBuilder<>();
    }

    public RequestBuilder<T, R> client(Class<T> clintInterface) {
        this.clintInterface = clintInterface;
        return this;
    }

    @Deprecated
    public RequestBuilder<T, R> nodeInfo(RegisterNodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        return this;
    }

    public RequestBuilder<T, R> nodeInfo(InstanceLiveInfo nodeInfo) {
        this.instanceLiveInfo = nodeInfo;
        return this;
    }

    public RequestBuilder<T, R> executorTimeout(Integer executorTimeout) {
        if (Objects.isNull(executorTimeout)) {
            return this;
        }

        Assert.isTrue(executorTimeout > 0, () -> new SnailJobServerException("executorTimeout can not less 0"));
        this.executorTimeout = executorTimeout;
        return this;
    }

    public RequestBuilder<T, R> failRetry(boolean failRetry) {
        this.failRetry = failRetry;
        return this;
    }

    public RequestBuilder<T, R> retryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public RequestBuilder<T, R> retryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public RequestBuilder<T, R> retryListener(SnailJobRetryListener retryListener) {
        this.retryListener = retryListener;
        return this;
    }

    public RequestBuilder<T, R> failover(boolean failover) {
        this.failover = failover;
        return this;
    }

    public RequestBuilder<T, R> routeKey(int routeKey) {
        this.routeKey = routeKey;
        return this;
    }

    public RequestBuilder<T, R> allocKey(String allocKey) {
        this.allocKey = allocKey;
        return this;
    }

    public T build() {
        if (Objects.isNull(clintInterface)) {
            throw new SnailJobServerException("clintInterface cannot be null");
        }

        Assert.notNull(instanceLiveInfo, () -> new SnailJobServerException("instanceLiveInfo cannot be null"));
        Assert.notNull(instanceLiveInfo.getNodeInfo(), () -> new SnailJobServerException("nodeInfo cannot be null"));
        Assert.notBlank(instanceLiveInfo.getNodeInfo().getNamespaceId(), () -> new SnailJobServerException("namespaceId cannot be null"));

        if (failover) {
            Assert.isTrue(routeKey > 0, () -> new SnailJobServerException("routeKey cannot be null"));
            Assert.notBlank(allocKey, () -> new SnailJobServerException("allocKey cannot be null"));
        }
        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw new SnailJobServerException("class not found exception to: [{}]", clintInterface.getName());
        }

        InvocationHandler invocationHandler = new GrpcClientInvokeHandlerV2(
                GrpcClientInvokeConfig.builder()
                .instanceLiveInfo(instanceLiveInfo)
                .failRetry(failRetry)
                .retryTimes(retryTimes)
                .retryInterval(retryInterval)
                .retryListener(retryListener)
                .routeKey(routeKey)
                .allocKey(allocKey)
                .failover(failover)
                .executorTimeout(executorTimeout)
                 .async(false)
                .build()
        );

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
                new Class[]{clintInterface}, invocationHandler);
    }

}
