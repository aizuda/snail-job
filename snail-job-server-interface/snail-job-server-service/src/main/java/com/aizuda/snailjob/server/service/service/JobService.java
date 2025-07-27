package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.model.base.JobRequest;
import com.aizuda.snailjob.model.base.JobResponse;
import com.aizuda.snailjob.model.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.base.JobTriggerRequest;

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

    Long addJob(JobRequest request);

    boolean updateJob(JobRequest jobRequestVO);

    boolean deleteJobByIds(Set<Long> ids);

    Boolean trigger(JobTriggerRequest jobTrigger);

    Boolean updateJobStatus(StatusUpdateRequest requestDTO);

    <T extends JobResponse> T getJobById(Long id, Class<T> clazz);
}
