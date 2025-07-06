package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.handler.AbstractTriggerHandler;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.Result;

public class TriggerWorkflowHandler extends AbstractTriggerHandler<TriggerWorkflowHandler,Boolean> {


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
        Result<Object> result;
        if (isOpenApiV2()) {
            result = clientV2.triggerWorkFlow(getReqDTO());
        } else {
            result = client.triggerWorkFlow(getReqDTO());
        }

        Assert.isTrue(StatusEnum.YES.getStatus() == result.getStatus(),
                () -> new SnailJobClientException(result.getMessage()));
        return (Boolean) result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getJobId() != null && !Long.valueOf(0).equals(getReqDTO().getJobId()), "triggerJobId cannot be null and must be greater than 0");
    }

    @Override
    public TriggerWorkflowHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
