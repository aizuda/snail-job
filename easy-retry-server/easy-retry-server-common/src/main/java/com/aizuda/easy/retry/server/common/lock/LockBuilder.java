package com.aizuda.easy.retry.server.common.lock;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;

import java.time.LocalDateTime;

/**
 * @author xiaowoniu
 * @date 2024-01-11 22:10:25
 * @since 2.6.0
 */
public final class LockBuilder {


    private String lockName;

    private boolean resident;

    public static LockBuilder newBuilder() {
        return new LockBuilder();
    }

    public LockBuilder withResident(String lockName) {
        this.lockName = lockName;
        resident = Boolean.TRUE;
        return this;
    }

    public LockBuilder withDisposable(String lockName) {
        this.lockName = lockName;
        resident = Boolean.FALSE;
       return this;
    }

    public LockProvider build() {
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockManager.initialize();
        LockManager.setLockName(lockName);
        if (resident) {
            return new ResidentLockProvider();
        } else {
            return new DisposableLockProvider();
        }
    }

}
