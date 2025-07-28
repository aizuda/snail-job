package com.aizuda.snailjob.server.openapi.api;

import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.model.response.JobBatchApiResponse;
import com.aizuda.snailjob.server.openapi.service.JobBatchApiService;
import com.aizuda.snailjob.server.service.service.JobBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Qualifier("jobBatchApiCommonService")
    private final JobBatchService jobBatchService;
    private final JobBatchApiService jobBatchApiService;

    @GetMapping(SystemConstants.HTTP_PATH.OPENAPI_GET_JOB_BATCH_DETAIL_V2)
    public JobBatchApiResponse getJobBatchByIds(@RequestParam("id") Long id) {
        return jobBatchService.getJobBatchById(id, JobBatchApiResponse.class);
    }
}
