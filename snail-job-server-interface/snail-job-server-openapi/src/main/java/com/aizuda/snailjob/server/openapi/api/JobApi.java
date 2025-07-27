package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.model.request.JobApiRequest;
import com.aizuda.snailjob.model.response.JobApiResponse;
import com.aizuda.snailjob.model.request.StatusUpdateApiRequest;
import com.aizuda.snailjob.model.request.JobTriggerApiRequest;
import com.aizuda.snailjob.server.openapi.service.JobApiService;
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
    private final JobApiService jobApiService;
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

}
