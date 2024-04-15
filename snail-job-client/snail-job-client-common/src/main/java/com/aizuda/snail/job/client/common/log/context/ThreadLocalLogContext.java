package com.aizuda.snail.job.client.common.log.context;

import cn.hutool.core.lang.Assert;
import com.aizuda.snail.job.client.common.SnailLogContext;
import com.aizuda.snail.job.client.common.exception.EasyRetryClientException;

/**
 * ThreadLocal实现类
 *
 * @author: xiaowoniu
 * @date : 2023-08-09 16:34
 * @since 3.2.0
 */
public class ThreadLocalLogContext<T> implements SnailLogContext<T> {

   private final ThreadLocal<T> threadLocal;

    public ThreadLocalLogContext(ThreadLocal<T> threadLocal) {
        Assert.notNull(threadLocal, ()-> new EasyRetryClientException("thread local can not be null"));
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
