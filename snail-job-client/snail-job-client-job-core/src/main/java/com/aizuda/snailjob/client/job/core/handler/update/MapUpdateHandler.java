package com.aizuda.snailjob.client.job.core.handler.update;


import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class MapUpdateHandler extends UpdateHandler<MapUpdateHandler> {

    public MapUpdateHandler(Long jobId) {
        super(JobTaskTypeEnum.MAP, jobId);
        setR(this);
    }

    @Override
    public MapUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
