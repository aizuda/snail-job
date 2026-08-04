package com.aizuda.snailjob.client.job.core.handler.trigger;

public class ShardingTriggerBizIdHandler extends TriggerJobBizIdHandler<ShardingTriggerBizIdHandler> {

    public ShardingTriggerBizIdHandler(String bizId) {
        super(bizId);
    }

    @Override
    public ShardingTriggerBizIdHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }
}