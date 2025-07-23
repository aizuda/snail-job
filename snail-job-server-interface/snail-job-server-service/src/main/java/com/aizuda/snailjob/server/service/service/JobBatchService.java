package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.JobBatchResponseBaseDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface JobBatchService {

    <T extends JobBatchResponseBaseDTO> T getJobBatchById(Long id,  Class<T> clazz);


}
