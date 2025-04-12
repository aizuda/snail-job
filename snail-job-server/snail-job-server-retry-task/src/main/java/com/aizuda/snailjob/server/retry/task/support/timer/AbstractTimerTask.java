package com.aizuda.snailjob.server.retry.task.support.timer;

import com.aizuda.snailjob.server.common.TimerTask;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author opensnail
 * @date 2023-09-23 11:10:01
 * @since 2.4.0
 */
@Slf4j
public abstract class AbstractTimerTask implements TimerTask<String> {

    protected String groupName;
    protected String namespaceId;
    protected Long retryId;
    protected Long retryTaskId;

    @Override
    public void run(Timeout timeout) throws Exception {
        log.debug("Start executing retry task. Current time:[{}] Group name:[{}] Retry ID:[{}] RetryTaskId:[{}] Namespace ID:[{}]",
                LocalDateTime.now(), groupName, retryId, retryTaskId, namespaceId);
        try {
            doRun(timeout);
        } catch (Exception e) {
            log.error("Retry task execution failed Group name:[{}] Retry ID:[{}] RetryTaskId:[{}] Namespace ID:[{}]",
                    groupName, retryId, retryTaskId, namespaceId, e);
        } finally {
            // 先清除时间轮的缓存
            RetryTimerWheel.clearCache(idempotentKey());

        }
    }

    protected abstract void doRun(Timeout timeout);
}
