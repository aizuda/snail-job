package com.aizuda.easy.retry.server.retry.task.support.timer;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * @author www.byteblogs.com
 * @date 2023-09-23 11:10:01
 * @since 2.4.0
 */
public abstract class AbstractTimerTask implements TimerTask {

    protected String groupName;
    protected String uniqueId;

    @Override
    public void run(Timeout timeout) throws Exception {

        // 先清除时间轮的缓存
        TimerWheelHandler.clearCache(groupName, uniqueId);

        doRun(timeout);
    }

    protected abstract void doRun(Timeout timeout);
}
