package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.exception.SnailJobClientException;

public class RequestTriggerJobHandler extends AbstractRequestHandler<Boolean>{
    private Long triggerJobId;
    // 1: job; 2: workflow
    private int triggerType;

    public RequestTriggerJobHandler(Long tiggerJobId, int triggerType) {
        this.triggerJobId = tiggerJobId;
        this.triggerType = triggerType;
    }

    @Override
    protected Boolean doExecute() {
        if (triggerType == 1) {
            return (Boolean) client.triggerJob(triggerJobId).getData();
        }
        if (triggerType == 2) {
            return (Boolean) client.triggerWorkFlow(triggerJobId).getData();
        }
        throw new SnailJobClientException("snail job openapi check error");
    }

    @Override
    protected boolean checkRequest() {
        return triggerJobId != null && !Long.valueOf(0).equals(triggerJobId);
    }
}
