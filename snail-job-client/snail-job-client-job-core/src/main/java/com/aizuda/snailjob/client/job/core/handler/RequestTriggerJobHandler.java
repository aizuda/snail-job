package com.aizuda.snailjob.client.job.core.handler;

import com.aizuda.snailjob.client.common.exception.SnailJobClientException;
import com.aizuda.snailjob.client.job.core.enums.JobTypeEnum;

public class RequestTriggerJobHandler extends AbstractRequestHandler<Boolean>{
    private final Long triggerJobId;
    // 1: job; 2: workflow
    private final int triggerType;

    public RequestTriggerJobHandler(Long triggerJobId, int triggerType) {
        this.triggerJobId = triggerJobId;
        this.triggerType = triggerType;
    }

    @Override
    protected Boolean doExecute() {
        if (triggerType == JobTypeEnum.JOB.getType()) {
            return (Boolean) client.triggerJob(triggerJobId).getData();
        }
        if (triggerType == JobTypeEnum.WORKFLOW.getType()) {
            return (Boolean) client.triggerWorkFlow(triggerJobId).getData();
        }
        throw new SnailJobClientException("snail job openapi check error");
    }

    @Override
    protected boolean checkRequest() {
        return triggerJobId != null && !Long.valueOf(0).equals(triggerJobId);
    }
}
