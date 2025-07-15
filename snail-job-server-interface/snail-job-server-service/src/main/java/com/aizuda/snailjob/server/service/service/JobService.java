package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.JobRequestBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobStatusUpdateRequestBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobTriggerBaseDTO;

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

    Boolean trigger(JobTriggerBaseDTO jobTrigger);

    Boolean updateJobStatus(JobStatusUpdateRequestBaseDTO requestDTO);

    JobResponseBaseDTO getJobById(Long id);
}
