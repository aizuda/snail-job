package com.aizuda.snailjob.client.job.core.handler.trigger;


public class MapTriggerHandler extends TriggerJobHandler<MapTriggerHandler>{

    public MapTriggerHandler(Long triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public MapTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
