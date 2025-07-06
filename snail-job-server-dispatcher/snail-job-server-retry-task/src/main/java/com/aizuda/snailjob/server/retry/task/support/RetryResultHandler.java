package com.aizuda.snailjob.server.retry.task.support;

import com.aizuda.snailjob.server.retry.task.support.result.RetryResultContext;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
public interface RetryResultHandler {

    boolean supports(RetryResultContext context);

    void handle(RetryResultContext context);
}
