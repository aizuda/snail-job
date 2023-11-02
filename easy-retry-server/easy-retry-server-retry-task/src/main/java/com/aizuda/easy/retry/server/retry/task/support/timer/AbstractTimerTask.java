package com.aizuda.easy.retry.server.retry.task.support.timer;

import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2023-09-23 11:10:01
 * @since 2.4.0
 */
@Slf4j
public abstract class AbstractTimerTask implements TimerTask {

    protected String groupName;
    protected String uniqueId;

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 10, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>());


    @Override
    public void run(Timeout timeout) throws Exception {
        executor.execute(() -> {
            log.info("开始执行重试任务. 当前时间:[{}] groupName:[{}] uniqueId:[{}]", LocalDateTime.now(), groupName,
                uniqueId);
            try {
                doRun(timeout);
            } catch (Exception e) {
                log.error("重试任务执行失败 groupName:[{}] uniqueId:[{}]", groupName, uniqueId, e);
            } finally {
                // 先清除时间轮的缓存
                RetryTimerWheel.clearCache(groupName, uniqueId);

            }
        });

    }

    protected abstract void doRun(Timeout timeout);
}
