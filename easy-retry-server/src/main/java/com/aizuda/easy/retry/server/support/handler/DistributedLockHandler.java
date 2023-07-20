package com.aizuda.easy.retry.server.support.handler;

import com.aizuda.easy.retry.server.persistence.mybatis.mapper.DistributedLockMapper;
import com.aizuda.easy.retry.server.persistence.support.LockAccess;
import com.aizuda.easy.retry.server.support.cache.CacheLockRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author www.byteblogs.com
 * @date 2023-07-20 22:28:35
 * @since 2.1.0
 */
@Component
public class DistributedLockHandler {
    @Autowired
    private LockAccess lockAccess;

    public boolean lock(String lockName) {
        // 先本地缓存锁定信息
        if (!CacheLockRecord.lockRecordRecentlyCreated(lockName)) {
            if (lockAccess.insertRecord()) {
                CacheLockRecord.addLockRecord(lockName);
                return true;
            }
            // we were not  to create the record, it already exists, let's put it to the cache so we do not try again
            CacheLockRecord.addLockRecord(lockName);
        }

        return lockAccess.updateRecord();
    }


}
