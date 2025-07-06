package com.aizuda.snailjob.server.job.task.support.stop;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import org.springframework.stereotype.Component;

/**
 * @author: opensnail
 * @date : 2024-06-19
 * @since : sj_1.1.0
 */
@Component
public class MapTaskStopHandler extends MapReduceTaskStopHandler {

    @Override
    public JobTaskTypeEnum getTaskType() {
        return JobTaskTypeEnum.MAP;
    }

    @Override
    protected void doStop(final TaskStopJobContext context) {
        super.doStop(context);
    }
}
