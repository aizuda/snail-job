package com.aizuda.snailjob.client.common.window;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.model.request.RetryTaskRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.window.LeapArray;
import com.aizuda.snailjob.common.core.window.Listener;
import com.aizuda.snailjob.common.core.window.WindowWrap;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 通过滑动窗口上报异常数据
 *
 * @author: opensnail
 * @date : 2022-05-28 15:07
 */
@Slf4j
public class RetryLeapArray extends LeapArray<ConcurrentLinkedQueue<RetryTaskRequest>> {

    protected List<Listener<RetryTaskRequest>> listenerList;

    public RetryLeapArray(int sampleCount, int intervalInMs) {
        super(sampleCount, intervalInMs);
    }

    public RetryLeapArray(int sampleCount, int intervalInMs, List<Listener<RetryTaskRequest>> listenerList) {
        super(sampleCount, intervalInMs);
        this.listenerList = listenerList;
    }

    public RetryLeapArray(int sampleCount, int intervalInMs, Listener<RetryTaskRequest> listener) {
        this(sampleCount, intervalInMs, Collections.singletonList(listener));
    }

    @Override
    public ConcurrentLinkedQueue<RetryTaskRequest> newEmptyBucket(long timeMillis) {
        return new ConcurrentLinkedQueue<>();
    }

    @Override
    protected WindowWrap<ConcurrentLinkedQueue<RetryTaskRequest>> resetWindowTo(WindowWrap<ConcurrentLinkedQueue<RetryTaskRequest>> windowWrap, long startTime) {

        ConcurrentLinkedQueue<RetryTaskRequest> deepCopy = new ConcurrentLinkedQueue<>(windowWrap.value());
        try {

            if (CollUtil.isNotEmpty(deepCopy)) {
                for (Listener<RetryTaskRequest> listener : listenerList) {
                    listener.handler(new ArrayList<>(deepCopy));
                }
            }
        } catch (Exception e) {
            log.error("Sliding window listener processing failed data:[{}]", JsonUtil.toJsonString(windowWrap.value()), e);
        }

        windowWrap.value().removeAll(deepCopy);
        windowWrap.resetTo(startTime);

        return windowWrap;
    }
}
