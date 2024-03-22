package com.aizuda.easy.retry.client.common;

/**
 * @author: xiaowoniu
 * @date : 2024-03-22
 * @since :3.2.0
 */
public interface EasyRetryLogContext<T>  {

    void set(T value);

    void remove();

    T get();

}
