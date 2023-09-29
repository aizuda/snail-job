package com.aizuda.easy.retry.server.common.client;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-09 09:24
 * @since : 2.4.0
 */
public class SimpleRetryListener implements RetryListener {

    @Override
    public <V> void onRetry(final Attempt<V> attempt) {

    }
}
