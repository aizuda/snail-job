package com.aizuda.snail.job.server.common.lock;

import com.aizuda.snail.job.server.common.dto.LockConfig;
import com.aizuda.snail.job.server.common.lock.persistence.LockStorage;
import com.aizuda.snail.job.server.common.lock.persistence.LockStorageFactory;

/**
 * @author xiaowoniu
 * @date 2024-01-11 21:26:54
 * @since 2.6.0
 */
public class DisposableLockProvider extends AbstractLockProvider {

    @Override
    protected boolean doLockAfter(final LockConfig lockConfig) {
        return Boolean.FALSE;
    }

    @Override
    protected void doUnlock(LockConfig lockConfig) {
        doUnlockWithDelete(lockConfig);
    }

    protected boolean doUnlockWithDelete(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        return lockStorage.releaseLockWithDelete(lockConfig.getLockName());
    }

    @Override
    protected boolean createLock(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        return lockStorage.createLock(lockConfig);
    }

    @Override
    protected boolean renewal(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        return lockStorage.renewal(lockConfig);
    }
}
