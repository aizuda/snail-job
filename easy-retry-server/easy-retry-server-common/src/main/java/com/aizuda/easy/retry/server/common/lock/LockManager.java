package com.aizuda.easy.retry.server.common.lock;

import com.aizuda.easy.retry.server.common.dto.LockConfig;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author xiaowoniu
 * @date 2024-01-11 22:06:02
 * @since 2.6.0
 */
public final class LockManager {
   private static final ThreadLocal<LockConfig> LOCK_CONFIG = new ThreadLocal<>();

   public static LockConfig getLockConfig() {
        return LOCK_CONFIG.get();
    }

    public static void initialize() {
        LOCK_CONFIG.set(new LockConfig());
    }

    public static void clear() {
        LOCK_CONFIG.remove();
    }

    public static void setLockName(String lockName) {
        getLockConfig().setLockName(lockName);
    }

    public static void setLockAtLeast(Duration lockAtLeast) {

        getLockConfig().setLockAtLeast(lockAtLeast);
    }

    public static void setCreateDt(LocalDateTime createDt) {
        getLockConfig().setCreateDt(createDt);
    }

    public static void setLockAtMost(Duration lockAtMost) {
        getLockConfig().setLockAtMost(lockAtMost);
    }


}
