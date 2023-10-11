package com.aizuda.easy.retry.server.common.client;

import cn.hutool.core.lang.Assert;
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
    private String groupName;
    private String hostId;
    private String hostIp;
    private Integer hostPort;
    private String contextPath;
    private boolean failRetry;
    private int retryTimes = 3;
    private int retryInterval = 1;
   private RetryListener retryListener = new SimpleRetryListener();


    public static <T, R> RequestBuilder<T, R> newBuilder() {
        return new RequestBuilder<>();
    }

    public RequestBuilder<T, R> client(Class<T> clintInterface) {
        this.clintInterface = clintInterface;
        return this;
    }

    public RequestBuilder<T, R> hostPort(Integer hostPort) {
        this.hostPort = hostPort;
        return this;
    }

    public RequestBuilder<T, R> contextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public RequestBuilder<T, R> hostId(String hostId) {
        this.hostId = hostId;
        return this;
    }

    public RequestBuilder<T, R> hostIp(String hostIp) {
        this.hostIp = hostIp;
        return this;
    }

    public RequestBuilder<T, R> groupName(String groupName) {
        this.groupName = groupName;
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


    public T build() {
        if (Objects.isNull(clintInterface)) {
            throw new EasyRetryServerException("clintInterface cannot be null");
        }

        Assert.notBlank(groupName, () -> new EasyRetryServerException("groupName cannot be null"));
        Assert.notBlank(hostId, () -> new EasyRetryServerException("hostId cannot be null"));
        Assert.notBlank(hostIp, () -> new EasyRetryServerException("hostIp cannot be null"));
        Assert.notNull(hostPort, () -> new EasyRetryServerException("hostPort cannot be null"));
        Assert.notBlank(contextPath, () -> new EasyRetryServerException("contextPath cannot be null"));

        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw new EasyRetryServerException("class not found exception to: [{}]", clintInterface.getName());
        }

        RpcClientInvokeHandler clientInvokeHandler = new RpcClientInvokeHandler(
            groupName, hostId, hostIp, hostPort, contextPath, failRetry, retryTimes, retryInterval, retryListener);

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
            new Class[]{clintInterface}, clientInvokeHandler);
    }

}
