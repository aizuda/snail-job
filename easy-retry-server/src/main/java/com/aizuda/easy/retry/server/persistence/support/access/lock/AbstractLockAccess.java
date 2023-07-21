package com.aizuda.easy.retry.server.persistence.support.access.lock;

import com.aizuda.easy.retry.server.dto.LockConfig;
import com.aizuda.easy.retry.server.persistence.support.LockAccess;
import com.aizuda.easy.retry.server.support.cache.CacheLockRecord;
import org.jetbrains.annotations.NotNull;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:46:14
 * @since 2.1.0
 */
public abstract class AbstractLockAccess implements LockAccess {

    @Override
    public boolean lock(final LockConfig lockConfig) {

        String lockName = lockConfig.getLockName();

        boolean tryToCreateLockRecord = CacheLockRecord.lockRecordRecentlyCreated(lockName);
        if (!tryToCreateLockRecord) {
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
        return updateRecord(lockConfig);
    }

    protected boolean doLock(@NotNull final LockConfig lockConfig) {
        return insertRecord(lockConfig);
    }

    protected abstract boolean insertRecord(@NotNull final LockConfig lockConfig);

    protected abstract boolean updateRecord(@NotNull final LockConfig lockConfig);
}
