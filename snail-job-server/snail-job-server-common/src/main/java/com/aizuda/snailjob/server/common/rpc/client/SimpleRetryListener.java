package com.aizuda.snailjob.server.common.rpc.client;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;

/**
 * @author: opensnail
 * @date : 2023-10-09 09:24
 * @since : 2.4.0
 */
public class SimpleRetryListener implements RetryListener {

    @Override
    public <V> void onRetry(final Attempt<V> attempt) {

    }
}
