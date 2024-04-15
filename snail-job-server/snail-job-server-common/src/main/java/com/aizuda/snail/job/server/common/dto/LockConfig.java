package com.aizuda.snail.job.server.common.dto;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 分布式锁配置
 *
 * @author: opensnail
 * @date : 2023-07-21 08:43
 * @since 2.1.0
 */
public class LockConfig {

    private LocalDateTime createDt;

    private String lockName;

    private Duration lockAtMost;

    private Duration lockAtLeast;

    public LocalDateTime getCreateDt() {
        return createDt;
    }

    public String getLockName() {
        return lockName;
    }

    public void setCreateDt(LocalDateTime createDt) {
        this.createDt = createDt;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public void setLockAtMost(Duration lockAtMost) {
        this.lockAtMost = lockAtMost;
    }

    public void setLockAtLeast(Duration lockAtLeast) {
        this.lockAtLeast = lockAtLeast;
    }

    public LocalDateTime getLockAtMost() {
        return createDt.plus(lockAtMost);
    }

    public LocalDateTime getLockAtLeast() {
        return createDt.plus(lockAtLeast);
    }
}
