package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.RetryResponseBaseDTO;
import com.aizuda.snailjob.server.service.dto.StatusUpdateRequestBaseDTO;
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

    <T extends RetryResponseBaseDTO> T getRetryById(Long id, Class<T> clazz);

    boolean triggerRetry(TriggerRetryDTO triggerRetryDTO);

    boolean updateRetryStatus(StatusUpdateRequestBaseDTO requestDTO);
}
