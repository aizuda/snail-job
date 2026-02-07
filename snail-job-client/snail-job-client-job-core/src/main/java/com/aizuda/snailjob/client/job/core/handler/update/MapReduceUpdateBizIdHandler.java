package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class MapReduceUpdateBizIdHandler extends UpdateBizIdHandler<MapReduceUpdateBizIdHandler> {

    public MapReduceUpdateBizIdHandler(String bizId) {
        super(bizId, JobTaskTypeEnum.MAP_REDUCE);
        setR(this);
    }

    @Override
    public MapReduceUpdateBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapReduceUpdateBizIdHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }

    @Override
    public MapReduceUpdateBizIdHandler setShardNum(Integer shardNum) {
        return super.setShardNum(shardNum);
    }
}