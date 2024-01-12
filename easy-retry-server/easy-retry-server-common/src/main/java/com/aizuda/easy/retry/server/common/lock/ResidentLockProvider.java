package com.aizuda.easy.retry.server.common.lock;

import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.server.common.lock.persistence.LockStorage;
import com.aizuda.easy.retry.server.common.lock.persistence.LockStorageFactory;

/**
 * @author xiaowoniu
 * @date 2024-01-11 21:26:54
 * @since 2.6.0
 */
public class ResidentLockProvider extends AbstractLockProvider {


    @Override
    protected void doUnlock(LockConfig lockConfig) {
        doUnlockWithUpdate(lockConfig);
    }

    protected void doUnlockWithUpdate(LockConfig lockConfig) {
        LockStorage lockStorage = LockStorageFactory.getLockStorage();
        lockStorage.releaseLockWithUpdate(lockConfig.getLockName(), lockConfig.getLockAtLeast());
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
