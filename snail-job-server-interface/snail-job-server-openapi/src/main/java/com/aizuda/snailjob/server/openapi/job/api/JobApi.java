package com.aizuda.snailjob.server.openapi.job.api;

import com.aizuda.snailjob.server.openapi.job.dto.JobRequestDTO;
import com.aizuda.snailjob.server.openapi.job.service.JobApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobApi {
    private final JobApiService jobApiService;

    @PostMapping("/add")
    public Long addJob(@Valid JobRequestDTO jobRequest) {
       return jobApiService.addJob(jobRequest);
    }
}
