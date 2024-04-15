package com.aizuda.snail.job.client.common;

/**
 * @author: xiaowoniu
 * @date : 2024-03-22
 * @since :3.2.0
 */
public interface SnailLogContext<T>  {

    void set(T value);

    void remove();

    T get();

}
