package com.aizuda.snailjob.client.core;

import com.aizuda.snailjob.client.core.intercepter.ThreadLockRetrySiteSnapshotContext;
import com.aizuda.snailjob.client.core.intercepter.ThreadLockRetrySiteSnapshotContext;

/**
 * 重试现场记录上下文
 * 默认实现see: {@link ThreadLockRetrySiteSnapshotContext}
 *
 * @author: opensnail
 * @date : 2023-08-09 16:25
 */
public interface RetrySiteSnapshotContext<T> {

    void set(T value);

    void remove();

    T get();
}
