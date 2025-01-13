package com.aizuda.snailjob.client.job.core.handler.trigger;


public class ShardingTriggerHandler extends TriggerJobHandler<ShardingTriggerHandler>{

    public ShardingTriggerHandler(Long triggerJobId) {
        super(triggerJobId);
    }

    @Override
    public ShardingTriggerHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }
}
