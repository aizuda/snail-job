package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class ShardingUpdateHandler extends UpdateHandler<ShardingUpdateHandler>{

    public ShardingUpdateHandler(Long jobId) {
        super(JobTaskTypeEnum.SHARDING, jobId);
        setR(this);
    }

    @Override
    protected ShardingUpdateHandler addShardingArgs(String[] shardingValue) {
        return super.addShardingArgs(shardingValue);
    }

    @Override
    protected ShardingUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
