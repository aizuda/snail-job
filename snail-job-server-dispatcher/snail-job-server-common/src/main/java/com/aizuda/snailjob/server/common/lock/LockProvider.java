package com.aizuda.snailjob.server.common.lock;


import java.time.Duration;

/**
 * @author opensnail
 * @date 2023-07-20 22:45:41
 * @since 2.1.0
 */
public interface LockProvider {

    boolean lock(Duration lockAtLeast, Duration lockAtMost);

    boolean lock(Duration lockAtMost);

    void unlock();

}
