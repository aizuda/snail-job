package com.aizuda.snailjob.client.job.core.handler.query;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.response.JobExistsResponse;

public class ExistsWorkflowBizIdHandler extends AbstractJobRequestHandler<JobExistsResponse> {

    private final String bizId;

    public ExistsWorkflowBizIdHandler(String bizId) {
        this.bizId = bizId;
    }

    @Override
    protected void afterExecute(JobExistsResponse response) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobExistsResponse doExecute() {
        Result<JobExistsResponse> result = clientV2.existsWorkflowByBizId(bizId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(bizId != null && !bizId.isEmpty(), "bizId cannot be null or empty");
    }

}