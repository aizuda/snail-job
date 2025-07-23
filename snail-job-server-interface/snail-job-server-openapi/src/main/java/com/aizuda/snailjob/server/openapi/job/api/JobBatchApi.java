package com.aizuda.snailjob.server.openapi.job.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.server.openapi.job.dto.JobBatchResponseDTO;
import com.aizuda.snailjob.server.openapi.job.service.JobBatchApiService;
import com.aizuda.snailjob.server.service.service.JobBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-06
 */
@RestController
@RequiredArgsConstructor
public class JobBatchApi {
    private final JobBatchService jobBatchApiService;
    private final JobBatchApiService apiService;

    @PostMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_JOB_BATCH_DETAIL_V2)
    public JobBatchResponseDTO getJobBatchByIds(@RequestParam("id") Long id) {
        return jobBatchApiService.getJobBatchById(id, JobBatchResponseDTO.class);
    }
}
