package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class MapUpdateBizIdHandler extends UpdateBizIdHandler<MapUpdateBizIdHandler> {

    public MapUpdateBizIdHandler(String bizId) {
        super(bizId, JobTaskTypeEnum.MAP);
        setR(this);
    }

    @Override
    public MapUpdateBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapUpdateBizIdHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}