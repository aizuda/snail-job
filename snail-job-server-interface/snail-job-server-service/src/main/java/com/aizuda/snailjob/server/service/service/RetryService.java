package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.RetryResponseDTO;
import com.aizuda.snailjob.model.base.StatusUpdateRequest;
import com.aizuda.snailjob.server.service.dto.TriggerRetryDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-25
 */
public interface RetryService {

    <T extends RetryResponseDTO> T getRetryById(Long id, Class<T> clazz);

    boolean triggerRetry(TriggerRetryDTO triggerRetryDTO);

    boolean updateRetryStatus(StatusUpdateRequest requestDTO);
}
