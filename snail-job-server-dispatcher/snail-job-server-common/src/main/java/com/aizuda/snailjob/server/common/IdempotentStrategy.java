package com.aizuda.snailjob.server.common;

/**
 * 幂等策略
 *
 * @author: opensnail
 * @date : 2021-11-23 09:20
 */
public interface IdempotentStrategy<T> {

    boolean set(T key);

    boolean isExist(T key);

    boolean clear(T key);

}
