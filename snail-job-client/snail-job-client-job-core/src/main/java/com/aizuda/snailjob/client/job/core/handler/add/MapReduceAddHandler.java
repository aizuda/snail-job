package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class MapReduceAddHandler extends AddHandler<MapReduceAddHandler> {

    public MapReduceAddHandler(Integer shardNum) {
        this(JobTaskTypeEnum.MAP_REDUCE, shardNum);
    }

    public MapReduceAddHandler(JobTaskTypeEnum taskType, Integer shardNum) {
        super(taskType, shardNum);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapReduceAddHandler addShardingArgs(String... shardingValue) {
        return super.addShardingArgs(shardingValue);
    }

    @Override
    public MapReduceAddHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
