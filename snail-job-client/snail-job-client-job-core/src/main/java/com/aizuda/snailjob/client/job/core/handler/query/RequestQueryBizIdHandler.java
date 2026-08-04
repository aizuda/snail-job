package com.aizuda.snailjob.client.job.core.handler.query;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.response.JobApiResponse;

/**
 * Query handler for job details by bizId.
 *
 * @author opensnail
 * @since sj_1.2.0
 */
public class RequestQueryBizIdHandler extends AbstractJobRequestHandler<JobApiResponse> {

    private final String bizId;

    public RequestQueryBizIdHandler(String bizId) {
        this.bizId = bizId;
    }

    @Override
    protected void afterExecute(JobApiResponse jobResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobApiResponse doExecute() {
        Result<JobApiResponse> result = clientV2.getJobDetailByBizId(bizId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(bizId != null && !bizId.isEmpty(), "bizId cannot be null or empty");
    }

}