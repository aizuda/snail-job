package com.aizuda.snailjob.client.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.model.response.RetryApiResponse;

import java.util.Objects;

public class QueryRetryHandler extends AbstractRetryRequestHandler<RetryApiResponse> {

    private final Long retryId;

    public QueryRetryHandler(Long retryId) {
        this.retryId = retryId;
    }

    @Override
    protected RetryApiResponse doExecute() {
        Result<Object> result;
        result = clientV2.queryRetryTask(retryId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        Object data = result.getData();
        if (Objects.isNull(data)) {
            return null;
        }
        return JsonUtil.parseObject(JsonUtil.toJsonString(data), RetryApiResponse.class);
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(retryId != null && retryId > 0, "retryId cannot be null and must be greater than 0");
    }
}
