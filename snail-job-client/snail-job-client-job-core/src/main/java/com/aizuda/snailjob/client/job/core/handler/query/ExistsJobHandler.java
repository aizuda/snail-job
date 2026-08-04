package com.aizuda.snailjob.client.job.core.handler.query;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.response.JobExistsResponse;

public class ExistsJobHandler extends AbstractJobRequestHandler<JobExistsResponse> {

    private final Long jobId;

    public ExistsJobHandler(Long jobId) {
        this.jobId = jobId;
    }

    @Override
    protected void afterExecute(JobExistsResponse response) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobExistsResponse doExecute() {
        Result<JobExistsResponse> result = clientV2.existsJobById(jobId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(jobId != null && !Long.valueOf(0).equals(jobId),
                "jobId cannot be null and must be greater than 0");
    }

}