package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.StatusUpdateRequestDTO;
import com.aizuda.snailjob.server.service.dto.JobTriggerDTO;

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

    boolean triggerWorkFlow(JobTriggerDTO jobTriggerDTO);

    boolean updateWorkFlowStatus(StatusUpdateRequestDTO requestDTO);

}
