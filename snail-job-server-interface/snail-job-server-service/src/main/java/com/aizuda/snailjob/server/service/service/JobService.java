package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.server.service.dto.JobRequestDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseDTO;
import com.aizuda.snailjob.server.service.dto.StatusUpdateRequestDTO;
import com.aizuda.snailjob.server.service.dto.JobTriggerDTO;

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

    Long addJob(JobRequestDTO request);

    boolean updateJob(JobRequestDTO jobRequestVO);

    boolean deleteJobByIds(Set<Long> ids);

    Boolean trigger(JobTriggerDTO jobTrigger);

    Boolean updateJobStatus(StatusUpdateRequestDTO requestDTO);

    <T extends JobResponseDTO> T getJobById(Long id, Class<T> clazz);
}
