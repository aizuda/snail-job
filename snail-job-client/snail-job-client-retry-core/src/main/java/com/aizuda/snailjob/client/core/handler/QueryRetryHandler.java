package com.aizuda.snailjob.client.core.handler;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.response.RetryApiResponse;

public class QueryRetryHandler extends AbstractRetryRequestHandler<RetryApiResponse> {

    private final Long retryId;

    public QueryRetryHandler(Long retryId) {
        this.retryId = retryId;
    }

    @Override
    protected RetryApiResponse doExecute() {
        Result<RetryApiResponse> result = clientV2.queryRetryTask(retryId);
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(retryId != null && retryId > 0, "retryId cannot be null and must be greater than 0");
    }
}
