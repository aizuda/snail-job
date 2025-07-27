package com.aizuda.snailjob.server.openapi.service.impl;

import com.aizuda.snailjob.model.base.JobRequest;
import com.aizuda.snailjob.server.openapi.service.JobApiService;
import com.aizuda.snailjob.server.openapi.util.OpenApiSessionUtils;
import com.aizuda.snailjob.model.base.JobResponse;
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
    protected void addJobPopulate(Job job, JobRequest request) {
    }

    @Override
    protected void addJobPreValidator(JobRequest request) {

    }

    @Override
    protected void getJobByIdAfter(JobResponse responseBaseDTO, Job job) {

    }

    @Override
    protected void updateJobPreValidator(JobRequest jobRequest) {

    }

    @Override
    protected String getNamespaceId() {
        return OpenApiSessionUtils.getNamespaceId();
    }

}
