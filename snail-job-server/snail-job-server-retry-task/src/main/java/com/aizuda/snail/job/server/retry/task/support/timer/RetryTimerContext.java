package com.aizuda.snail.job.server.retry.task.support.timer;

import com.aizuda.snail.job.server.retry.task.support.dispatch.task.TaskExecutorSceneEnum;
import lombok.Data;

/**
 * @author opensnail
 * @date 2023-09-23 09:14:03
 * @since 2.4.0
 */
@Data
public class RetryTimerContext {

    private String namespaceId;

    private String groupName;

    private String uniqueId;

    private TaskExecutorSceneEnum scene;

}
