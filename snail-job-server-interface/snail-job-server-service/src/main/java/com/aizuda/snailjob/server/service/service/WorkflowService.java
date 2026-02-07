package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.model.request.WorkflowStatusUpdateBizIdRequest;
import com.aizuda.snailjob.model.request.WorkflowTriggerBizIdRequest;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.request.base.WorkflowTriggerRequest;
import com.aizuda.snailjob.model.response.WorkflowExistsResponse;

import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
public interface WorkflowService {

    boolean deleteWorkflowByIds(Set<Long> ids);

    boolean triggerWorkFlow(WorkflowTriggerRequest request);

    boolean updateWorkFlowStatus(StatusUpdateRequest requestDTO);

    WorkflowExistsResponse existsWorkflowById(Long id);

    // ==================== bizId 接口 ====================

    boolean deleteWorkflowByBizIds(Set<String> bizIds);

    boolean triggerWorkflowByBizId(WorkflowTriggerBizIdRequest request);

    boolean updateWorkflowStatusByBizId(WorkflowStatusUpdateBizIdRequest request);

    WorkflowExistsResponse existsWorkflowByBizId(String bizId);
}
