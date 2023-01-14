package com.x.retry.client.core.window;

import com.x.retry.common.core.util.JsonUtil;
import com.x.retry.common.core.window.LeapArray;
import com.x.retry.common.core.window.Listener;
import com.x.retry.common.core.window.WindowWrap;
import com.x.retry.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: www.byteblogs.com
 * @date : 2022-05-28 15:07
 */
@Slf4j
public class RetryLeapArray extends LeapArray<ConcurrentLinkedQueue<RetryTaskDTO>> {

    protected List<Listener<RetryTaskDTO>> listenerList;

    /**
     * The total bucket count is: {@code sampleCount = intervalInMs / windowLengthInMs}.
     *
     * @param sampleCount  bucket count of the sliding window
     * @param intervalInMs the total time interval of this {@link LeapArray} in milliseconds
     */
    public RetryLeapArray(int sampleCount, int intervalInMs) {
        super(sampleCount, intervalInMs);
    }

    public RetryLeapArray(int sampleCount, int intervalInMs, List<Listener<RetryTaskDTO>> listenerList) {
        super(sampleCount, intervalInMs);
        this.listenerList = listenerList;
    }

    public RetryLeapArray(int sampleCount, int intervalInMs, Listener<RetryTaskDTO> listener) {
        this(sampleCount, intervalInMs, Collections.singletonList(listener));
    }

    @Override
    public ConcurrentLinkedQueue<RetryTaskDTO> newEmptyBucket(long timeMillis) {
        return new ConcurrentLinkedQueue<>();
    }

    @Override
    protected WindowWrap<ConcurrentLinkedQueue<RetryTaskDTO>> resetWindowTo(WindowWrap<ConcurrentLinkedQueue<RetryTaskDTO>> windowWrap, long startTime) {

        ConcurrentLinkedQueue<RetryTaskDTO> deepCopy = new ConcurrentLinkedQueue<>(windowWrap.value());
        try {

            if (!CollectionUtils.isEmpty(deepCopy)) {
                for (Listener<RetryTaskDTO> listener : listenerList) {
                    listener.handler(new ArrayList<>(deepCopy));
                }
            }
        } catch (Exception e) {
            log.error("滑动窗口监听器处理失败 data:[{}]", JsonUtil.toJsonString(windowWrap.value()), e);
        }

        windowWrap.value().removeAll(deepCopy);
        windowWrap.resetTo(startTime);

        return windowWrap;
    }
}
