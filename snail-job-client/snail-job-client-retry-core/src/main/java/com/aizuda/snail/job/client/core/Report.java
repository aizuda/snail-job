package com.aizuda.snail.job.client.core;

/**
 * @author opensnail
 * @date 2023-05-15
 * @since 2.0
 */
public interface Report {

    boolean supports(boolean async);

    boolean report(String scene, final String targetClassName, final Object[] args);
}
