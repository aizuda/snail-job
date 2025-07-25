package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.WorkflowDetailResponseDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface WorkflowBatchService {

    <T extends WorkflowDetailResponseDTO> T getWorkflowBatchById(Long id, Class<T> clazz);
}
