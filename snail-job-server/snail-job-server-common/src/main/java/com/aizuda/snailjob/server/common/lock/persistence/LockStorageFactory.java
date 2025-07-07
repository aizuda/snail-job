package com.aizuda.snailjob.server.common.lock.persistence;

import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.utils.DbUtils;
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
                .filter(lockProvider -> lockProvider.supports(db.getName()))
                .findFirst().orElseThrow(() -> new SnailJobServerException("Suitable lock handler not found"));
    }

}
