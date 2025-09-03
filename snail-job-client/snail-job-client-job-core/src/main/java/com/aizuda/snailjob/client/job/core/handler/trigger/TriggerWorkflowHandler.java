package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractWorkflowTriggerHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

public class TriggerWorkflowHandler extends AbstractWorkflowTriggerHandler<TriggerWorkflowHandler,Boolean> {

    public TriggerWorkflowHandler(Long triggerJobId) {
        super(triggerJobId);
        setR(this);
    }

    @Override
    protected void afterExecute(Boolean aBoolean) {

    }

    @Override
    protected void beforeExecute() {

    }

    @Override
    protected Boolean doExecute() {
        Result<Object> result = clientV2.triggerWorkFlow(getReqDTO());
        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getWorkflowId() != null && !Long.valueOf(0).equals(getReqDTO().getWorkflowId()), "triggerWorkFlow cannot be null and must be greater than 0");
    }

    @Override
    public TriggerWorkflowHandler addContext(String argsKey, Object argsValue) {
        return super.addContext(argsKey, argsValue);
    }
}
