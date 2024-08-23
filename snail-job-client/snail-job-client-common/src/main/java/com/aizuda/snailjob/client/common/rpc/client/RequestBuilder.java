package com.aizuda.snailjob.client.common.rpc.client;

import com.aizuda.snailjob.client.common.config.SnailJobProperties;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.core.model.Result;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 构建请求类型
 *
 * @author: opensnail
 * @date : 2023-05-12 16:47
 * @since 1.3.0
 */
public class RequestBuilder<T, R extends Result<Object>> {

    private Class<T> clintInterface;

    private Consumer<R> callback;

    private boolean async = true;

    private long timeout = 60 * 1000;

    private TimeUnit unit = TimeUnit.MILLISECONDS;

    public static <T, R extends Result<Object>> RequestBuilder<T, R> newBuilder() {
        return new RequestBuilder<>();
    }

    public RequestBuilder<T, R> client(Class<T> clintInterface) {
        this.clintInterface = clintInterface;
        return this;
    }

    public RequestBuilder<T, R> callback(Consumer<R> callback) {
        this.callback = callback;
        return this;
    }

    public RequestBuilder<T, R> async(boolean async) {
        this.async = async;
        return this;
    }

    public RequestBuilder<T, R> timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RequestBuilder<T, R> unit(TimeUnit unit) {
        this.unit = unit;
        return this;
    }

    public T build() {
        if (Objects.isNull(clintInterface)) {
            throw new SnailJobClientException("clintInterface cannot be null");
        }

        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw new SnailJobClientException("class not found exception to: [{}]", clintInterface.getName());
        }

        InvocationHandler invocationHandler;
        SnailJobProperties properties = SnailSpringContext.getBean(SnailJobProperties.class);
        RpcTypeEnum rpcType = properties.getRpcType();
        if (Objects.isNull(rpcType) || RpcTypeEnum.NETTY == rpcType) {
            invocationHandler= new RpcClientInvokeHandler<>(async, timeout, unit,
                callback);
        } else {
            invocationHandler = new GrpcClientInvokeHandler<>(async, timeout, unit, callback);
        }

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
            new Class[]{clintInterface}, invocationHandler);
    }

}
