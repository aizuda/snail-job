package com.aizuda.snailjob.client.core.handler;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.util.ValidatorUtils;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.model.request.TriggerRetryApiRequest;

public class TriggerRetryHandler extends AbstractRetryRequestHandler<Boolean> {

    private final TriggerRetryApiRequest triggerRetryDTO;

    public TriggerRetryHandler(Long retryId) {
        this.triggerRetryDTO = new TriggerRetryApiRequest();
        triggerRetryDTO.setId(retryId);
    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.triggerRetryTask(triggerRetryDTO);;
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return ValidatorUtils.validateEntity(triggerRetryDTO);
    }
}
