package com.aizuda.easy.retry.server.retry.task.support.dispatch.actor;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-22 17:09
 */
@Slf4j
public class CallbackTimerTask implements TimerTask {

    @Override
    public void run(final Timeout timeout) throws Exception {
        log.info("回调任务执行");
    }
}
