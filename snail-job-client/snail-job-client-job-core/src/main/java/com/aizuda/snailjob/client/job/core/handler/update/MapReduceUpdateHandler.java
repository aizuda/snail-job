package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class MapReduceUpdateHandler extends UpdateHandler<MapReduceUpdateHandler> {

    public MapReduceUpdateHandler(Long jobId) {
        super(JobTaskTypeEnum.MAP_REDUCE, jobId);
        setR(this);
    }

    @Override
    public MapReduceUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapReduceUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }

    @Override
    public MapReduceUpdateHandler setShardNum(Integer shardNum) {
        return super.setShardNum(shardNum);
    }
}
