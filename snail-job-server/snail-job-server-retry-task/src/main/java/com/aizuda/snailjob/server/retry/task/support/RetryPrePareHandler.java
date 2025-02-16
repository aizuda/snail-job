package com.aizuda.snailjob.server.retry.task.support;

import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-25
 */
public interface RetryPrePareHandler {

    boolean matches(Integer status);

    void handle(RetryTaskPrepareDTO jobPrepareDTO);
}
