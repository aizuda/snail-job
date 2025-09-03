package com.aizuda.snailjob.client.job.core.handler.query;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractJobRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.response.JobBatchApiResponse;

import java.util.Objects;

/**
 * @since 1.5.0
 */
public class RequestQueryJobBatchHandler extends AbstractJobRequestHandler<JobBatchApiResponse> {
    private final Long queryJobBatchId;

    public RequestQueryJobBatchHandler(Long queryJobBatchId) {
        this.queryJobBatchId = queryJobBatchId;
    }

    @Override
    protected void afterExecute(JobBatchApiResponse jobBatchResponseVO) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected JobBatchApiResponse doExecute() {
        Result<JobBatchApiResponse> result = clientV2.getJobBatchDetail(queryJobBatchId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        JobBatchApiResponse data = result.getData();
        if (Objects.isNull(data)) {
            return null;
        }
        return data;
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(queryJobBatchId != null && !Long.valueOf(0).equals(queryJobBatchId), "queryJobBatchId cannot be null and must be greater than 0");
    }

}
