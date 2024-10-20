package com.aizuda.snailjob.client.job.core.handler.update;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

public class ClusterUpdateHandler extends UpdateHandler<ClusterUpdateHandler> {

    public ClusterUpdateHandler(Long jobId) {
        super(JobTaskTypeEnum.CLUSTER, jobId);
        setR(this);
    }

    @Override
    public ClusterUpdateHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        return super.setRouteKey(algorithmEnum);
    }

    @Override
    public ClusterUpdateHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
