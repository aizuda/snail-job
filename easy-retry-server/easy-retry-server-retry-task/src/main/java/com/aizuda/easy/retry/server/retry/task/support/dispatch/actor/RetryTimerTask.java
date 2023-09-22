package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor;

import com.aizuda.easy.retry.server.retry.task.support.retry.RetryExecutor;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:09
 */
@Data
@Slf4j
public class RetryTimerTask implements TimerTask {

    private RetryExecutor executor;

    @Override
    public void run(final Timeout timeout) throws Exception {
        log.info("重试任务执行");
//        RetryContext retryContext = executor.getRetryContext();
//        RetryTask retryTask = retryContext.getRetryTask();
    }
}
