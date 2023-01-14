package com.x.retry.client.core;

import com.github.rholder.retry.RetryListener;
import com.google.common.base.Predicate;
import com.x.retry.client.core.retryer.RetryerInfo;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 09:03
 */
public interface RetryExecutorParameter<BR, SR> {

    Predicate<Throwable> exceptionPredicate();

    BR backOff();

    SR stop();

    List<RetryListener> getRetryListeners();

}
