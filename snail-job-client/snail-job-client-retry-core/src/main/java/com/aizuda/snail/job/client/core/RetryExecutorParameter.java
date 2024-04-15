package com.aizuda.snail.job.client.core;

import com.github.rholder.retry.RetryListener;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-03-04 09:03
 */
public interface RetryExecutorParameter<BR, SR> {

    BR backOff();

    SR stop();

    List<RetryListener> getRetryListeners();

}
