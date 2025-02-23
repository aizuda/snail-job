package com.aizuda.snailjob.server.retry.task.support.timer;

import lombok.Data;

/**
 * @author opensnail
 * @date 2023-09-23 09:14:03
 * @since 2.4.0
 */
@Data
public class RetryTimerContext {

    private Long retryId;

    private Long retryTaskId;

    private Integer retryTaskExecutorScene;

}
