package com.aizuda.easy.retry.server.job.task.support;

import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.aizuda.easy.retry.server.job.task.support.callback.ClientCallbackContext;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 23:10:50
 * @since 2.4.0
 */
public interface ClientCallbackHandler {

    TaskTypeEnum getTaskInstanceType();

    void callback(ClientCallbackContext context);
}
