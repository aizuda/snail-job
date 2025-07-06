package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.common.vo.WorkflowDetailResponseVO;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface WorkflowBatchService {

    WorkflowDetailResponseVO getWorkflowBatchById(Long id);
}
