package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class BroadcastUpdateBizIdHandler extends UpdateBizIdHandler<BroadcastUpdateBizIdHandler> {

    public BroadcastUpdateBizIdHandler(String bizId) {
        super(bizId, JobTaskTypeEnum.BROADCAST);
        setR(this);
    }

    @Override
    public BroadcastUpdateBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}