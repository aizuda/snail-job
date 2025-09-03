package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractTriggerHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

public abstract class TriggerJobHandler<H> extends AbstractTriggerHandler<H, Boolean> {

    public TriggerJobHandler(Long triggerJobId) {
        super(triggerJobId);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Boolean> result = clientV2.triggerJob(getReqDTO());;
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getJobId() != null && !Long.valueOf(0).equals(getReqDTO().getJobId()),  "triggerJobId cannot be null and must be greater than 0");
    }
}
