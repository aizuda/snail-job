package com.aizuda.easy.retry.server.common.lock;


import com.aizuda.easy.retry.server.common.dto.LockConfig;

import java.time.Duration;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:45:41
 * @since 2.1.0
 */
public interface LockProvider {

    boolean lock(Duration lockAtLeast, Duration lockAtMost);

    boolean lock(Duration lockAtMost);

    boolean unlock();

}
