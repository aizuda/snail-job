package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class ClusterAddHandler extends AddHandler<ClusterAddHandler> {

    public ClusterAddHandler(JobTaskTypeEnum taskType) {
        this(taskType, null);
    }

    public ClusterAddHandler(JobTaskTypeEnum taskType, Integer shardNum) {
        super(taskType, shardNum);
        // 集群模式只允许并发为 1
        setParallelNum(1);
        setR(this);
    }

    @Override
    public ClusterAddHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        super.setRouteKey(algorithmEnum);
        return this;
    }

    @Override
    public ClusterAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
