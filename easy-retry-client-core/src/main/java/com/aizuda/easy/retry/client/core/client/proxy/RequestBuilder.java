package com.aizuda.easy.retry.client.core.client.proxy;

import com.aizuda.easy.retry.client.core.exception.EasyRetryClientException;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 构建请求类型
 *
 * @author: www.byteblogs.com
 * @date : 2023-05-12 16:47
 * @since 1.3.0
 */
public class RequestBuilder<T, R> {

    private Class<T> clintInterface;

    private Consumer<R> callback;

    private boolean async = true;

    private long timeout = 60*1000;

    private TimeUnit unit = TimeUnit.MILLISECONDS;

    public static <T, R> RequestBuilder<T, R> newBuilder() {
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
            throw new EasyRetryClientException("clintInterface cannot be null");
        }

        try {
            clintInterface = (Class<T>) Class.forName(clintInterface.getName());
        } catch (Exception e) {
            throw new EasyRetryClientException("class not found exception to: [{}]", clintInterface.getName());
        }

        ClientInvokeHandler<R> clientInvokeHandler = new ClientInvokeHandler<>(async, timeout, unit, callback);

        return (T) Proxy.newProxyInstance(clintInterface.getClassLoader(),
            new Class[]{clintInterface}, clientInvokeHandler);
    }

}
