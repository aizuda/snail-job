package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.model.base.JobBatchResponse;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface JobBatchService {

    <T extends JobBatchResponse> T getJobBatchById(Long id, Class<T> clazz);


}
