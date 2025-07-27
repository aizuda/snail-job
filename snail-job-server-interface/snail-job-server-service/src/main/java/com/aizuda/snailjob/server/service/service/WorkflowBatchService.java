package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.model.response.base.WorkflowDetailResponse;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface WorkflowBatchService {

    <T extends WorkflowDetailResponse> T getWorkflowBatchById(Long id, Class<T> clazz);
}
