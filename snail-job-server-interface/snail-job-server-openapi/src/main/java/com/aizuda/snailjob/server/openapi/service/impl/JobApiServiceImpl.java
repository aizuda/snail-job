package com.aizuda.snailjob.server.openapi.service.impl;

import com.aizuda.snailjob.server.openapi.service.JobApiService;
import com.aizuda.snailjob.server.openapi.util.OpenApiSessionUtils;
import com.aizuda.snailjob.server.service.dto.JobRequestDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseDTO;
import com.aizuda.snailjob.server.service.service.impl.AbstractJobService;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Service("jobApiCommonService")
@RequiredArgsConstructor
public class JobApiServiceImpl extends AbstractJobService implements JobApiService {

    @Override
    protected void addJobPopulate(Job job, JobRequestDTO request) {
    }

    @Override
    protected void addJobPreValidator(JobRequestDTO request) {

    }

    @Override
    protected void getJobByIdAfter(JobResponseDTO responseBaseDTO, Job job) {

    }

    @Override
    protected void updateJobPreValidator(JobRequestDTO jobRequest) {

    }

    @Override
    protected String getNamespaceId() {
        return OpenApiSessionUtils.getNamespaceId();
    }

}
