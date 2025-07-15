package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;
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

    WorkflowDetailResponseBaseDTO getWorkflowBatchById(Long id);
}
