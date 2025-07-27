package com.aizuda.snailjob.client.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.base.StatusUpdateRequest;

public class UpdateRetryStatusHandler extends AbstractRetryRequestHandler<Boolean> {

    private final StatusUpdateRequest updateRetryStatusDTO;

    public UpdateRetryStatusHandler(Long retryId) {
        this.updateRetryStatusDTO = new StatusUpdateRequest();
        updateRetryStatusDTO.setId(retryId);
    }

    public UpdateRetryStatusHandler setRetryStatus(RetryStatusEnum retryStatusEnum) {
        updateRetryStatusDTO.setRetryStatus(retryStatusEnum.getStatus());
        updateRetryStatusDTO.setStatus(retryStatusEnum.getStatus());
        return this;
    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result;
        if (isOpenApiV2()) {
            result = clientV2.updateRetryTaskStatus(updateRetryStatusDTO);
        } else {
            result = client.updateRetryTaskStatus(updateRetryStatusDTO);
        }
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(updateRetryStatusDTO);
    }
}
