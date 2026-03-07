package com.aizuda.snailjob.client.job.core.handler.trigger;

import cn.hutool.core.lang.Pair;
import com.aizuda.snailjob.client.job.core.handler.AbstractWorkflowBizIdTriggerHandler;
import com.aizuda.snailjob.common.core.model.Result;

/**
 * Handler for triggering workflow by bizId.
 *
 * @author opensnail
 * @since sj_1.10.0
 */
public class TriggerWorkflowBizIdHandler extends AbstractWorkflowBizIdTriggerHandler<TriggerWorkflowBizIdHandler, Boolean> {

    public TriggerWorkflowBizIdHandler(String bizId) {
        super(bizId);
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
        Result<Boolean> result = clientV2.triggerWorkflowByBizId(getReqDTO());
        return result.getData();
    }

    @Override
    protected Pair<Boolean, String> checkRequest() {
        return Pair.of(getReqDTO().getBizId() != null && !getReqDTO().getBizId().isEmpty(),
                "bizId cannot be null or empty");
    }

    @Override
    public TriggerWorkflowBizIdHandler addContext(String argsKey, Object argsValue) {
        return super.addContext(argsKey, argsValue);
    }
}