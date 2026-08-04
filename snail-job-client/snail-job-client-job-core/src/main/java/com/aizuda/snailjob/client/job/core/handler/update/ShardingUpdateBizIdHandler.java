package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class ShardingUpdateBizIdHandler extends UpdateBizIdHandler<ShardingUpdateBizIdHandler> {

    public ShardingUpdateBizIdHandler(String bizId) {
        super(bizId, JobTaskTypeEnum.SHARDING);
        setR(this);
    }

    @Override
    public ShardingUpdateBizIdHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }

    @Override
    public ShardingUpdateBizIdHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}