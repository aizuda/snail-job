package com.aizuda.easy.retry.server.common.lock;

import com.aizuda.easy.retry.server.common.cache.CacheLockRecord;
import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;

import java.util.Arrays;
import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:46:14
 * @since 2.1.0
 */
public abstract class AbstractLockProvider implements LockProvider {
    protected final static List<String> ALLOW_DB =  Arrays.asList(DbTypeEnum.MYSQL.getDb(),
            DbTypeEnum.MARIADB.getDb(),
            DbTypeEnum.POSTGRES.getDb());
    @Override
    public boolean lock(final LockConfig lockConfig) {

        String lockName = lockConfig.getLockName();

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
        return updateRecord(lockConfig);
    }

    protected boolean doLock(final LockConfig lockConfig) {
        return insertRecord(lockConfig);
    }

    protected abstract boolean insertRecord(final LockConfig lockConfig);

    protected abstract boolean updateRecord(final LockConfig lockConfig);
}
