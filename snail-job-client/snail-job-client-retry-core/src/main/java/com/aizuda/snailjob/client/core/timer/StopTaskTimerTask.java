package com.aizuda.snailjob.client.core.timer;

import com.aizuda.snailjob.client.core.cache.FutureCache;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * @author opensnail
 * @date 2025-02-18
 * @since 1.4.0
 */
public class StopTaskTimerTask implements TimerTask {

    private Long retryTaskId;

    public StopTaskTimerTask(Long retryTaskId) {
        this.retryTaskId = retryTaskId;
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        FutureCache.remove(retryTaskId);
    }
}
