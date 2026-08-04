package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class MapReduceAddBizIdHandler extends AddBizIdHandler<MapReduceAddBizIdHandler> {

    public MapReduceAddBizIdHandler(String bizId) {
        this(bizId, JobTaskTypeEnum.MAP_REDUCE);
    }

    public MapReduceAddBizIdHandler(String bizId, JobTaskTypeEnum taskType) {
        super(bizId, taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapReduceAddBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapReduceAddBizIdHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }

    @Override
    public MapReduceAddBizIdHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        super.setRouteKey(algorithmEnum);
        return this;
    }

    @Override
    public MapReduceAddBizIdHandler setShardNum(Integer shardNum) {
        return super.setShardNum(shardNum);
    }
}