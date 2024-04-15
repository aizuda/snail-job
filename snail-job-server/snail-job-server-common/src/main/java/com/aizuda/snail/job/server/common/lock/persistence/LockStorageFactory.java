package com.aizuda.snail.job.server.common.lock.persistence;

import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import com.aizuda.snail.job.template.datasource.enums.DbTypeEnum;
import com.aizuda.snail.job.template.datasource.utils.DbUtils;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2024-01-11 22:38:36
 * @since 2.6.0
 */
public final class LockStorageFactory {

    private static final List<LockStorage> LOCK_STORAGES = Lists.newArrayList();

    public static void registerLockStorage(LockStorage lockStorage) {
        LOCK_STORAGES.add(lockStorage);
    }

    public static LockStorage getLockStorage() {
        DbTypeEnum db = DbUtils.getDbType();
        return LOCK_STORAGES.stream()
                .filter(lockProvider -> lockProvider.supports(db.getDb()))
                .findFirst().orElseThrow(() -> new EasyRetryServerException("未找到合适锁处理器"));
    }

}
