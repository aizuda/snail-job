package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.1.0
 */
public class BroadcastAddHandler extends AddHandler<BroadcastAddHandler> {

    public BroadcastAddHandler() {
        this(JobTaskTypeEnum.BROADCAST);
    }

    public BroadcastAddHandler(JobTaskTypeEnum taskType) {
        super(taskType);
        // 广播模式只允许并发为 1
        setParallelNum(1);
        // 广播模式采用轮询模式
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public BroadcastAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }
}
