package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.common.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.service.dto.JobRequestBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseDTO;
import com.aizuda.snailjob.server.service.dto.JobStatusUpdateRequestDTO;

import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
public interface JobService {

    Long addJob(JobRequestBaseDTO request);

    boolean updateJob(JobRequestBaseDTO jobRequestVO);

    boolean deleteJobByIds(Set<Long> ids);

    Boolean trigger(JobTriggerDTO jobTrigger);

    Boolean updateJobStatus(JobStatusUpdateRequestDTO requestDTO);

    JobResponseDTO getJobById(Long id);
}
