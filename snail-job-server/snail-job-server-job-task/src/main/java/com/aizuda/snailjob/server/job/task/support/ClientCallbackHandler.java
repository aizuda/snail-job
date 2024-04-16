package com.aizuda.snailjob.server.job.task.support;

import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackContext;
import com.aizuda.snailjob.server.job.task.support.callback.ClientCallbackContext;

/**
 * @author opensnail
 * @date 2023-10-03 23:10:50
 * @since 2.4.0
 */
public interface ClientCallbackHandler {

    JobTaskTypeEnum getTaskInstanceType();

    void callback(ClientCallbackContext context);
}
