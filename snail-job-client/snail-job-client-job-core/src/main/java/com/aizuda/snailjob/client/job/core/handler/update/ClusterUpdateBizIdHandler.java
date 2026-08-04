package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class ClusterUpdateBizIdHandler extends UpdateBizIdHandler<ClusterUpdateBizIdHandler> {

    public ClusterUpdateBizIdHandler(String bizId) {
        super(bizId, JobTaskTypeEnum.CLUSTER);
        setR(this);
    }

    @Override
    public ClusterUpdateBizIdHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        return super.setRouteKey(algorithmEnum);
    }

    @Override
    public ClusterUpdateBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}