package com.aizuda.easy.retry.server.support;

/**
 * 幂等策略
 *
 * @author: www.byteblogs.com
 * @date : 2021-11-23 09:20
 */
public interface IdempotentStrategy<T, V> {

    boolean set(T key, V value);

    V get(T t);

    boolean isExist(T key, V value);

    boolean clear(T key, V value);

}
