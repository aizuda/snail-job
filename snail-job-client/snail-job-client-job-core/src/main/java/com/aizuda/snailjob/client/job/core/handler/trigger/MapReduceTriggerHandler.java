package com.aizuda.snailjob.client.job.core.handler.trigger;


public class MapReduceTriggerHandler extends TriggerJobHandler<MapReduceTriggerHandler>{

    public MapReduceTriggerHandler(Long triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public MapReduceTriggerHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
