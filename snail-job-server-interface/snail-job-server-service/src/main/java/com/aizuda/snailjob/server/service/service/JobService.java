package com.aizuda.snailjob.server.service.service;

import com.aizuda.snailjob.model.request.JobBizIdApiRequest;
import com.aizuda.snailjob.model.request.JobTriggerBizIdRequest;
import com.aizuda.snailjob.model.request.StatusUpdateBizIdRequest;
import com.aizuda.snailjob.model.request.base.JobRequest;
import com.aizuda.snailjob.model.response.JobExistsResponse;
import com.aizuda.snailjob.model.response.base.JobResponse;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;
import com.aizuda.snailjob.model.request.base.JobTriggerRequest;

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

    JobExistsResponse existsJobById(Long id);

    // ==================== bizId 接口 ====================

    Long addJobByBizId(JobBizIdApiRequest request);

    <T extends JobResponse> T getJobByBizId(String bizId, Class<T> clazz);

    boolean deleteJobByBizIds(Set<String> bizIds);

    Boolean triggerByBizId(JobTriggerBizIdRequest request);

    Boolean updateJobStatusByBizId(StatusUpdateBizIdRequest request);

    boolean updateJobByBizId(JobBizIdApiRequest jobBizIdApiRequest);

    JobExistsResponse existsJobByBizId(String bizId);
}