package com.aizuda.snailjob.client.job.core.handler.add;

import com.aizuda.snailjob.client.job.core.enums.AllocationAlgorithmEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-10-19 12:25:49
 * @since sj_1.2.0
 */
public class MapAddHandler extends AddHandler<MapAddHandler> {

    public MapAddHandler() {
        this(JobTaskTypeEnum.MAP, null);
    }

    public MapAddHandler(JobTaskTypeEnum taskType, Integer shardNum) {
        super(taskType, shardNum);
        setRouteKey(AllocationAlgorithmEnum.ROUND);
        setR(this);
    }

    @Override
    public MapAddHandler addArgsStr(String argsKey, Object argsValue) {
        return super.addArgsStr(argsKey, argsValue);
    }

    @Override
    public MapAddHandler setParallelNum(Integer parallelNum) {
        return super.setParallelNum(parallelNum);
    }
}
