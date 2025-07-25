package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.JobBatchResponseDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface JobBatchService {

    <T extends JobBatchResponseDTO> T getJobBatchById(Long id, Class<T> clazz);


}
