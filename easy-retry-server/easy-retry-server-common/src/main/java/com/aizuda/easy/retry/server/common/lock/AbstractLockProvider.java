package com.aizuda.easy.retry.server.common.lock;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.common.cache.CacheLockRecord;
import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;

import java.time.Duration;

/**
 * @author www.byteblogs.com
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

        Assert.notNull(lockAtMost, () -> new EasyRetryServerException("lockAtMost can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtMost.isNegative(), () -> new EasyRetryServerException("lockAtMost  is negative. lockName:[{}]", lockName));
        Assert.notNull(lockAtLeast, () -> new EasyRetryServerException("lockAtLeast can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtLeast.compareTo(lockAtMost) > 0, () -> new EasyRetryServerException("lockAtLeast is longer than lockAtMost for lock. lockName:[{}]", lockName));

        LockManager.setLockAtLeast(lockAtLeast);
        LockManager.setLockAtLeast(lockAtMost);

        boolean tryToCreateLockRecord = !CacheLockRecord.lockRecordRecentlyCreated(lockName);
        if (tryToCreateLockRecord) {
            if (doLock(lockConfig)) {
                CacheLockRecord.addLockRecord(lockName);
                return true;
            }

            CacheLockRecord.addLockRecord(lockName);
        }

        try {
            return doLockAfter(lockConfig);
        } catch (Exception e) {
            if (tryToCreateLockRecord) {
                CacheLockRecord.remove(lockName);
            }

            throw e;
        }
    }

    protected boolean doLockAfter(LockConfig lockConfig) {
        return renewal(lockConfig);
    }

    protected boolean doLock(final LockConfig lockConfig) {
        return createLock(lockConfig);
    }

    @Override
    public boolean unlock() {
        try {
            LockConfig lockConfig = LockManager.getLockConfig();
            return doUnlock(lockConfig);
        } finally {
            LockManager.clear();
        }

    }

    protected abstract boolean doUnlock(LockConfig lockConfig);

    protected abstract boolean createLock(final LockConfig lockConfig);

    protected abstract boolean renewal(final LockConfig lockConfig);
}
