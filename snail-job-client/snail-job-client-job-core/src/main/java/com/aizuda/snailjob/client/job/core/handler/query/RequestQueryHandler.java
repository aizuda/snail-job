package com.aizuda.snailjob.client.job.core.handler.query;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.response.JobApiResponse;

import java.util.Objects;

public class RequestQueryHandler extends AbstractJobRequestHandler<JobApiResponse> {
    private final Long queryJobId;

    public RequestQueryHandler(Long queryJobId) {
        this.queryJobId = queryJobId;
    }

    @Override
    protected void afterExecute(JobApiResponse jobResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobApiResponse doExecute() {
        Result<JobApiResponse> result = clientV2.getJobDetail(queryJobId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(queryJobId != null && !Long.valueOf(0).equals(queryJobId), "queryJobId cannot be null and must be greater than 0");
    }

}
