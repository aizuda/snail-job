package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class MapAddBizIdHandler extends AddBizIdHandler<MapAddBizIdHandler> {

    public MapAddBizIdHandler(String bizId) {
        this(bizId, JobTaskTypeEnum.MAP);
    }

    public MapAddBizIdHandler(String bizId, JobTaskTypeEnum taskType) {
        super(bizId, taskType);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapAddBizIdHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapAddBizIdHandler setRouteKey(AllocationAlgorithmEnum algorithmEnum) {
        super.setRouteKey(algorithmEnum);
        return this;
    }

    @Override
    public MapAddBizIdHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}