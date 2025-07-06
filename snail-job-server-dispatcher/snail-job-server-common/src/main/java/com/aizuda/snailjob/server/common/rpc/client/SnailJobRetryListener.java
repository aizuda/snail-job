package com.aizuda.snailjob.server.common.rpc.client;

import com.github.rholder.retry.RetryListener;

import java.util.Map;

/**
 * author: zhangshuguang
 * date: 2025-02-17
 */
public interface SnailJobRetryListener extends RetryListener {

    /**
     * 传递属性信息
     *
     * @return Map<String, Object>
     */
    Map<String, Object> properties();

}
