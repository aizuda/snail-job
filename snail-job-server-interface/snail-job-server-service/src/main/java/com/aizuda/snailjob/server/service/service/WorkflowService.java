package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.common.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.service.dto.JobStatusUpdateRequestDTO;

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

    boolean updateWorkFlowStatus(JobStatusUpdateRequestDTO requestDTO);

}
