package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

public class TriggerJobHandler extends AbstractRequestHandler<Boolean> {
    private final Long triggerJobId;


    public TriggerJobHandler(Long triggerJobId) {
        this.triggerJobId = triggerJobId;
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result = client.triggerJob(triggerJobId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean)result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(triggerJobId != null && !Long.valueOf(0).equals(triggerJobId),  "triggerJobId不能为null并且必须大于0");
    }
}
