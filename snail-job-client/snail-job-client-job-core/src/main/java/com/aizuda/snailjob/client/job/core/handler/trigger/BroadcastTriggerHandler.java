package com.aizuda.snailjob.client.job.core.handler.trigger;


public class BroadcastTriggerHandler extends TriggerJobHandler<BroadcastTriggerHandler>{

    public BroadcastTriggerHandler(Long triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public BroadcastTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
