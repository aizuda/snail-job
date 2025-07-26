package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.server.openapi.dto.JobRequestApiDTO;
import com.aizuda.snailjob.server.openapi.dto.JobResponseApiDTO;
import com.aizuda.snailjob.server.openapi.dto.StatusUpdateRequestApiDTO;
import com.aizuda.snailjob.server.openapi.dto.JobTriggerApiDTO;
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
    public Long addJob(@RequestBody @Validated JobRequestApiDTO jobRequest) {
        return jobService.addJob(jobRequest);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB)
    public boolean updateJob(@RequestBody @Validated JobRequestApiDTO jobRequest) {
        return jobService.updateJob(jobRequest);
    }

    @DeleteMapping(SystemConstants.HTTP_PATH.OPENAPI_DELETE_JOB_V2)
    public boolean deleteJobByIds(@RequestBody
                                  @NotEmpty(message = "ids cannot be null")
                                  @Size(max = 100, message = "Maximum {max} deletions") Set<Long> ids) {
        return jobService.deleteJobByIds(ids);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_JOB_V2)
    public Boolean trigger(@RequestBody @Validated JobTriggerApiDTO jobTrigger) {
        return jobService.trigger(jobTrigger);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB_STATUS_V2)
    public Boolean updateJobStatus(@RequestBody @Validated StatusUpdateRequestApiDTO requestDTO) {
        return jobService.updateJobStatus(requestDTO);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_JOB_DETAIL_V2)
    public JobResponseApiDTO getJobById(@RequestParam("id") Long id) {
        return jobService.getJobById(id, JobResponseApiDTO.class);
    }

}
