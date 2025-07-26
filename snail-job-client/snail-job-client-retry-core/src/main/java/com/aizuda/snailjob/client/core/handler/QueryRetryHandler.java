package com.aizuda.snailjob.client.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.core.dto.RetryDTO;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;

import java.util.Objects;

public class QueryRetryHandler extends AbstractRetryRequestHandler<RetryDTO> {

    private final Long retryId;

    public QueryRetryHandler(Long retryId) {
        this.retryId = retryId;
    }

    @Override
    protected RetryDTO doExecute() {
        Result<Object> result;
        if (isOpenApiV2()){
            result = clientV2.queryRetryTask(retryId);
        } else {
            result = client.queryRetryTask(retryId);
        }
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        Object data = result.getData();
        Assert.isTrue(Objects.nonNull(data), () -> new SnailJobClientException("Failed to get details of task [{}]", retryId));
        return JsonUtil.parseObject(JsonUtil.toJsonString(data), RetryDTO.class);
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(retryId != null && retryId > 0, "retryId cannot be null and must be greater than 0");
    }
}
