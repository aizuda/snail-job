package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class ClusterAddBizIdHandler extends AddBizIdHandler<ClusterAddBizIdHandler> {

    public ClusterAddBizIdHandler(String bizId) {
        this(bizId, JobTaskTypeEnum.CLUSTER);
    }

    public ClusterAddBizIdHandler(String bizId, JobTaskTypeEnum taskType) {
        super(bizId, taskType);
        // 集群模式只允许并发为 1
        setParallelNum(1);
        setR(this);
    }

    @Override
    public ClusterAddBizIdHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        super.setRouteKey(algorithmEnum);
        return this;
    }

    @Override
    public ClusterAddBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}