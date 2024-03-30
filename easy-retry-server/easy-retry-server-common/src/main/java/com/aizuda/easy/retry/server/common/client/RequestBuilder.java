package com.aizuda.easy.retry.server.common.client;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.github.rholder.retry.RetryListener;

import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 构建请求类型
 *
 * @author: www.byteblogs.com
 * @date : 2023-06-19 16:47
 * @since 2.0.0
 */
public class RequestBuilder<T, R> {

    private Class<T> clintInterface;
    private RegisterNodeInfo nodeInfo;
    private boolean failRetry;
    private int retryTimes = 3;
    private int retryInterval = 1;
    private RetryListener retryListener = new SimpleRetryListener();
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

    public RequestBuilder<T, R> nodeInfo(RegisterNodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
        return this;
    }

    public RequestBuilder<T, R> executorTimeout(Integer executorTimeout) {
        if (Objects.isNull(executorTimeout)) {
            return this;
        }

        Assert.isTrue(executorTimeout > 0, () -> new EasyRetryServerException("executorTimeout can not less 0"));
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

    public RequestBuilder<T, R> retryListener(RetryListener retryListener) {
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
            throw new EasyRetryServerException("clintInterface cannot be null");
        }

        Assert.notNull(nodeInfo, () -> new EasyRetryServerException("nodeInfo cannot be null"));
        Assert.notBlank(nodeInfo.getNamespaceId(), () -> new EasyRetryServerException("namespaceId cannot be null"));

        if (failover) {
            Assert.isTrue(routeKey > 0, () -> new EasyRetryServerException("routeKey cannot be null"));
            Assert.notBlank(allocKey, () -> new EasyRetryServerException("allocKey cannot be null"));
        }
        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw new EasyRetryServerException("class not found exception to: [{}]", clintInterface.getName());
        }

        RpcClientInvokeHandler clientInvokeHandler = new RpcClientInvokeHandler(
                nodeInfo.getGroupName(), nodeInfo, failRetry, retryTimes, retryInterval,
                retryListener, routeKey, allocKey, failover, executorTimeout, nodeInfo.getNamespaceId());

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
                new Class[]{clintInterface}, clientInvokeHandler);
    }

}
