package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractRequestHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

public class TriggerWorkflowHandler extends AbstractRequestHandler<Boolean> {
    private final Long triggerJobId;

    public TriggerWorkflowHandler(Long triggerJobId) {
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
        Result<Object> result = client.triggerWorkFlow(triggerJobId);
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(triggerJobId != null && !Long.valueOf(0).equals(triggerJobId), "triggerJobId不能为null并且必须大于0");
    }
}
