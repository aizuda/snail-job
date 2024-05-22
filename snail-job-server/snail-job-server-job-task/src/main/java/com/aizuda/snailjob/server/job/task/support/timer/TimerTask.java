package com.aizuda.snailjob.server.job.task.support.timer;

/**
 * @author: opensnail
 * @date : 2024-05-21
 * @since : sj_1.0.0
 */
public interface TimerTask<T> extends io.netty.util.TimerTask {

    T getUniqueId();
}
