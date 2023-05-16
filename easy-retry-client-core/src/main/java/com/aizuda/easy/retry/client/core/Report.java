package com.aizuda.easy.retry.client.core;

import com.aizuda.easy.retry.client.core.retryer.RetryerInfo;

/**
 * @author www.byteblogs.com
 * @date 2023-05-15
 * @since 2.0
 */
public interface Report {

    boolean supports(boolean async);

    boolean report(String scene, final String targetClassName, final Object[] args);
}
