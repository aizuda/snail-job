package com.aizuda.snailjob.server.openapi.job.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.server.openapi.job.dto.JobRequestDTO;
import com.aizuda.snailjob.server.openapi.job.dto.JobResponseDTO;
import com.aizuda.snailjob.server.openapi.job.dto.StatusUpdateRequestDTO;
import com.aizuda.snailjob.server.openapi.job.dto.JobTriggerDTO;
import com.aizuda.snailjob.server.openapi.job.service.JobApiService;
import com.aizuda.snailjob.server.service.service.JobService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
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
    private final JobApiService apiService;
    private final JobService jobApiService;

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_ADD_JOB)
    public Long addJob(@RequestBody @Validated JobRequestDTO jobRequest) {
        return jobApiService.addJob(jobRequest);
    }

    @PutMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB)
    public boolean updateJob(@RequestBody @Validated JobRequestDTO jobRequest) {
        return jobApiService.updateJob(jobRequest);
    }

    @DeleteMapping(SystemConstants.HTTP_PATH.OPENAPI_DELETE_JOB_V2)
    public boolean deleteJobByIds(@RequestBody
                                  @NotEmpty(message = "ids cannot be null")
                                  @Size(max = 100, message = "Maximum {max} deletions") Set<Long> ids) {
        return jobApiService.deleteJobByIds(ids);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_TRIGGER_JOB_V2)
    public Boolean trigger(@RequestBody @Validated JobTriggerDTO jobTrigger) {
        return jobApiService.trigger(jobTrigger);
    }

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_UPDATE_JOB_STATUS_V2)
    public Boolean updateJobStatus(@RequestBody @Validated StatusUpdateRequestDTO requestDTO) {
        return jobApiService.updateJobStatus(requestDTO);
    }

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_JOB_DETAIL_V2)
    public JobResponseDTO getJobById(@RequestParam("id") Long id) {
        return jobApiService.getJobById(id, JobResponseDTO.class);
    }

}
