package com.aizuda.snailjob.client.common.threadlocal;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.client.common.SnailThreadLocal;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;

/**
 * ThreadLocal实现类
 *
 * @author: opensnail
 * @date : 2024-06-27
 * @since sj_1.1.0
 */
public class CommonThreadLocal<T> implements SnailThreadLocal<T> {

    private final ThreadLocal<T> threadLocal;

    public CommonThreadLocal(ThreadLocal<T> threadLocal) {
        Assert.notNull(threadLocal, () -> new SnailJobClientException("thread local can not be null"));
        this.threadLocal = threadLocal;
    }

    @Override
    public void set(T value) {
        threadLocal.set(value);
    }

    @Override
    public void remove() {
        threadLocal.remove();
    }

    @Override
    public T get() {
        return threadLocal.get();
    }
}
