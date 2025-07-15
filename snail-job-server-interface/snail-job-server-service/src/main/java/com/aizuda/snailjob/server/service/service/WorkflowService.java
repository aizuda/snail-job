package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.JobStatusUpdateRequestBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobTriggerBaseDTO;

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

    boolean triggerWorkFlow(JobTriggerBaseDTO jobTriggerDTO);

    boolean updateWorkFlowStatus(JobStatusUpdateRequestBaseDTO requestDTO);

}
