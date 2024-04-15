package com.aizuda.snail.job.client.common.window;

import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.common.core.window.LeapArray;
import com.aizuda.snail.job.common.core.window.Listener;
import com.aizuda.snail.job.common.core.window.WindowWrap;
import com.aizuda.snail.job.server.model.dto.RetryTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

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
public class RetryLeapArray extends LeapArray<ConcurrentLinkedQueue<RetryTaskDTO>> {

    protected List<Listener<RetryTaskDTO>> listenerList;

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
