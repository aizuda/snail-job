package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractTriggerBizIdHandler;
import com.aizuda.snailjob.common.core.model.Result;

public abstract class TriggerJobBizIdHandler<H> extends AbstractTriggerBizIdHandler<H, Boolean> {

    public TriggerJobBizIdHandler(String bizId) {
        super(bizId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.triggerJobByBizId(getReqDTO());
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getBizId() != null && !getReqDTO().getBizId().isEmpty(),  "bizId cannot be null or empty");
    }
}