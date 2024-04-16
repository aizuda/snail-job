package com.aizuda.snailjob.server.common.lock;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.server.common.cache.CacheLockRecord;
import com.aizuda.snailjob.server.common.dto.LockConfig;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.cache.CacheLockRecord;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author opensnail
 * @date 2023-07-20 22:46:14
 * @since 2.1.0
 */
public abstract class AbstractLockProvider implements LockProvider {

    @Override
    public boolean lock(Duration lockAtMost) {
        return lock(lockAtMost, lockAtMost);
    }

    @Override
    public boolean lock(Duration lockAtLeast, Duration lockAtMost) {
        LockConfig lockConfig = LockManager.getLockConfig();
        String lockName = lockConfig.getLockName();

        Assert.notNull(lockAtMost,
            () -> new SnailJobServerException("lockAtMost can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtMost.isNegative(),
            () -> new SnailJobServerException("lockAtMost  is negative. lockName:[{}]", lockName));
        Assert.notNull(lockAtLeast,
            () -> new SnailJobServerException("lockAtLeast can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtLeast.compareTo(lockAtMost) > 0,
            () -> new SnailJobServerException("lockAtLeast is longer than lockAtMost for lock. lockName:[{}]",
                lockName));

        LockManager.setCreateDt(LocalDateTime.now());
        LockManager.setLockAtLeast(lockAtLeast);
        LockManager.setLockAtMost(lockAtMost);

        boolean tryToCreateLockRecord = !CacheLockRecord.lockRecordRecentlyCreated(lockName);
        if (tryToCreateLockRecord) {
            if (doLock(lockConfig)) {
                CacheLockRecord.addLockRecord(lockName);
                return true;
            }
        }

        return doLockAfter(lockConfig);
    }

    protected abstract boolean doLockAfter(LockConfig lockConfig);

    protected boolean doLock(final LockConfig lockConfig) {
        return createLock(lockConfig);
    }

    @Override
    public void unlock() {
        try {
            LockConfig lockConfig = LockManager.getLockConfig();
            Assert.notNull(lockConfig, () -> new SnailJobServerException("lockConfig can not be null."));
            doUnlock(lockConfig);
        } finally {
            LockManager.clear();
        }

    }

    protected abstract void doUnlock(LockConfig lockConfig);

    protected abstract boolean createLock(final LockConfig lockConfig);

    protected abstract boolean renewal(final LockConfig lockConfig);
}
