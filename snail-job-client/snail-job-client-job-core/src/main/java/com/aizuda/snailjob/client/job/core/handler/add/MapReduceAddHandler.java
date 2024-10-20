package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.client.job.core.handler.update.MapReduceUpdateHandler;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class MapReduceAddHandler extends AddHandler<MapReduceAddHandler> {

    public MapReduceAddHandler() {
        this(JobTaskTypeEnum.MAP_REDUCE);
    }

    public MapReduceAddHandler(JobTaskTypeEnum taskType) {
        super(taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapReduceAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapReduceAddHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }

    @Override
    public MapReduceAddHandler setShardNum(Integer shardNum) {
        return super.setShardNum(shardNum);
    }
}
