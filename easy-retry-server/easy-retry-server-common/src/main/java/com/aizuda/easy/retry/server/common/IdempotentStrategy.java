package com.aizuda.easy.retry.server.common;

/**
 * 幂等策略
 *
 * @author: opensnail
 * @date : 2021-11-23 09:20
 */
public interface IdempotentStrategy<T, V> {

    boolean set(T key, V value);

    V get(T t);

    boolean isExist(T key, V value);

    boolean clear(T key, V value);

}
