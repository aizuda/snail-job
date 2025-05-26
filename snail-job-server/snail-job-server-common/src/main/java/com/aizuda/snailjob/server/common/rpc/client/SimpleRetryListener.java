package com.aizuda.snailjob.server.common.rpc.client;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author: opensnail
 * @date : 2023-10-09 09:24
 * @since : 2.4.0
 */
public class SimpleRetryListener implements SnailJobRetryListener {

    @Override
    public <V> void onRetry(final Attempt<V> attempt) {

    }

    @Override
    public Map<String, Object> properties() {
        return Maps.newHashMap();
    }
}
