package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.model.request.*;
import com.aizuda.snailjob.model.response.JobApiResponse;
import com.aizuda.snailjob.model.response.JobExistsResponse;
import com.aizuda.snailjob.server.service.service.JobService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@RestController
@RequiredArgsConstructor
public class JobApi {
    @Qualifier("jobApiCommonService")
    private final JobService jobService;

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_ADD_JOB)
    public Long addJob(@RequestBody @Validated JobApiRequest jobRequest) {
        return jobService.addJob(jobRequest);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB)
    public boolean updateJob(@RequestBody @Validated JobApiRequest jobRequest) {
        return jobService.updateJob(jobRequest);
    }

    @DeleteMapping(SystemConstants.HTTP_PATH.OPENAPI_DELETE_JOB_V2)
    public boolean deleteJobByIds(@RequestBody
                                  @NotEmpty(message = "ids cannot be null")
                                  @Size(max = 100, message = "Maximum {max} deletions") Set<Long> ids) {
        return jobService.deleteJobByIds(ids);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_JOB_V2)
    public Boolean trigger(@RequestBody @Validated JobTriggerApiRequest jobTrigger) {
        return jobService.trigger(jobTrigger);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB_STATUS_V2)
    public Boolean updateJobStatus(@RequestBody @Validated StatusUpdateApiRequest requestDTO) {
        return jobService.updateJobStatus(requestDTO);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_JOB_DETAIL_V2)
    public JobApiResponse getJobById(@RequestParam("id") Long id) {
        return jobService.getJobById(id, JobApiResponse.class);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_EXISTS_JOB_BY_ID)
    public JobExistsResponse existsJobById(@RequestParam("id") Long id) {
        return jobService.existsJobById(id);
    }

    // ==================== job bizId 接口 ====================

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_ADD_JOB_BY_BIZ_ID)
    public Long addJobByBizId(@RequestBody @Validated JobBizIdApiRequest jobRequest) {
        return jobService.addJobByBizId(jobRequest);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB_BY_BIZ_ID)
    public boolean updateJobByBizId(@RequestBody @Validated JobBizIdApiRequest jobRequest) {
        return jobService.updateJobByBizId(jobRequest);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_JOB_DETAIL_BY_BIZ_ID)
    public JobApiResponse getJobByBizId(@RequestParam("bizId") String bizId) {
        return jobService.getJobByBizId(bizId, JobApiResponse.class);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_JOB_BY_BIZ_ID)
    public Boolean triggerByBizId(@RequestBody @Validated JobTriggerBizIdRequest request) {
        return jobService.triggerByBizId(request);
    }

    @DeleteMapping(SystemConstants.HTTP_PATH.OPENAPI_DELETE_JOB_BY_BIZ_IDS)
    public Boolean deleteJobByBizIds(@RequestBody @Validated DeleteBizIdRequest request) {
        return jobService.deleteJobByBizIds(request.getBizIds());
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB_STATUS_BY_BIZ_ID)
    public Boolean updateJobStatusByBizId(@RequestBody @Validated StatusUpdateBizIdRequest request) {
        return jobService.updateJobStatusByBizId(request);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_EXISTS_JOB_BY_BIZ_ID)
    public JobExistsResponse existsJobByBizId(@RequestParam("bizId") String bizId) {
        return jobService.existsJobByBizId(bizId);
    }

}
