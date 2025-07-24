package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.WorkflowDetailResponseBaseDTO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface WorkflowBatchService {

    <T extends WorkflowDetailResponseBaseDTO> T getWorkflowBatchById(Long id, Class<T> clazz);
}
