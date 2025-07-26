package com.aizuda.snailjob.client.core.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.client.core.dto.RequestTriggerRetryDTO;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

public class TriggerRetryHandler extends AbstractRetryRequestHandler<Boolean> {

    private final RequestTriggerRetryDTO triggerRetryDTO;

    public TriggerRetryHandler(Long retryId) {
        this.triggerRetryDTO = new RequestTriggerRetryDTO();
        triggerRetryDTO.setId(retryId);
    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result;
        if (isOpenApiV2()){
            result = clientV2.triggerRetryTask(triggerRetryDTO);
        } else {
            result = client.triggerRetryTask(triggerRetryDTO);
        }
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(triggerRetryDTO);
    }
}
