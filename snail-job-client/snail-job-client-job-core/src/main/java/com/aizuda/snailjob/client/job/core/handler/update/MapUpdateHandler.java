package com.aizuda.snailjob.client.job.core.handler.update;


public class MapUpdateHandler extends UpdateHandler<MapUpdateHandler> {

    public MapUpdateHandler(Long jobId) {
        super(jobId);
        setR(this);
    }

    @Override
    protected MapUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    protected MapUpdateHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
