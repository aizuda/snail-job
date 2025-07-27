package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.model.response.base.RetryResponse;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.request.base.TriggerRetryRequest;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-25
 */
public interface RetryService {

    <T extends RetryResponse> T getRetryById(Long id, Class<T> clazz);

    boolean triggerRetry(TriggerRetryRequest triggerRetryRequest);

    boolean updateRetryStatus(StatusUpdateRequest requestDTO);
}
