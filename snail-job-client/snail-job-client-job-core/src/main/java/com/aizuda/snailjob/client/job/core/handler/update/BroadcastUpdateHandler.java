package com.aizuda.snailjob.client.job.core.handler.update;

public class BroadcastUpdateHandler extends UpdateHandler<BroadcastUpdateHandler>{

    public BroadcastUpdateHandler(Long jobId) {
        super(jobId);
        setR(this);
    }

    @Override
    public BroadcastUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
