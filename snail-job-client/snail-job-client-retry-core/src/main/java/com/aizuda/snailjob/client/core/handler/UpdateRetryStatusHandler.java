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
        updateRetryStatusDTO.setStatus(retryStatusEnum.getStatus());
        return this;
    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.updateRetryTaskStatus(updateRetryStatusDTO);;
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(updateRetryStatusDTO);
    }
}
