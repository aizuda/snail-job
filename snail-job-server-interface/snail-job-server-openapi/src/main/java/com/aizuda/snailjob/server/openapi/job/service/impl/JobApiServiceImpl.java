package com.aizuda.snailjob.server.openapi.job.service.impl;

import com.aizuda.snailjob.server.openapi.job.service.JobApiService;
import com.aizuda.snailjob.server.openapi.util.OpenApiSessionUtils;
import com.aizuda.snailjob.server.service.dto.JobRequestBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseBaseDTO;
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
@Service("jobApiService")
@RequiredArgsConstructor
public class JobApiServiceImpl extends AbstractJobService implements JobApiService {

    @Override
    protected void addJobPopulate(Job job, JobRequestBaseDTO request) {
    }

    @Override
    protected void addJobPreValidator(JobRequestBaseDTO request) {

    }

    @Override
    protected void getJobByIdAfter(JobResponseBaseDTO responseBaseDTO, Job job) {

    }

    @Override
    protected void updateJobPreValidator(JobRequestBaseDTO jobRequest) {

    }

    @Override
    protected String getNamespaceId() {
        return OpenApiSessionUtils.getNamespaceId();
    }

}
