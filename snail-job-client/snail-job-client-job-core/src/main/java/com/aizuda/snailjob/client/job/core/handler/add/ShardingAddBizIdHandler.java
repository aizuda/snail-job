package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class ShardingAddBizIdHandler extends AddBizIdHandler<ShardingAddBizIdHandler> {

    public ShardingAddBizIdHandler(String bizId) {
        this(bizId, JobTaskTypeEnum.SHARDING);
    }

    public ShardingAddBizIdHandler(String bizId, JobTaskTypeEnum taskType) {
        super(bizId, taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public ShardingAddBizIdHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }

    @Override
    public ShardingAddBizIdHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}