package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import org.springframework.stereotype.Component;

/**
 * @author opensnail
 * @date 2024-06-24 22:51:20
 * @since sj_1.1.0
 */
@Component
public class MapClientCallbackHandler extends MapReduceClientCallbackHandler {
    public MapClientCallbackHandler(JobTaskMapper jobTaskMapper) {
        super(jobTaskMapper);
    }

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP;
    }

    @Override
    protected void doCallback(ClientCallbackContext context) {
        super.doCallback(context);
    }
}
