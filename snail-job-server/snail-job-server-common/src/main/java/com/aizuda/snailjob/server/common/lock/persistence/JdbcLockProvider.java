package com.aizuda.snailjob.server.common.lock.persistence;

import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.cache.CacheLockRecord;
import com.aizuda.snailjob.server.common.dto.LockConfig;
import com.aizuda.snailjob.server.common.register.ServerRegister;
import com.aizuda.snailjob.template.datasource.enums.DbTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.mapper.DistributedLockMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.DistributedLock;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 基于DB实现的分布式锁
 *
 * @author: opensnail
 * @date : 2023-07-21 08:34
 * @since 2.1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class JdbcLockProvider implements LockStorage, Lifecycle {

    protected static final List<String> ALLOW_DB = Arrays.asList(
            DbTypeEnum.MYSQL.getDb(),
            DbTypeEnum.MARIADB.getDb(),
            DbTypeEnum.POSTGRES.getDb(),
            DbTypeEnum.ORACLE.getDb(),
            DbTypeEnum.SQLSERVER.getDb(),
            DbTypeEnum.DM.getDb());

    private final DistributedLockMapper distributedLockMapper;
    private final PlatformTransactionManager platformTransactionManager;

    @Override
    public boolean supports(final String storageMedium) {
        return ALLOW_DB.contains(storageMedium);
    }

    @Override
    public boolean createLock(LockConfig lockConfig) {
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            try {
                LocalDateTime now = lockConfig.getCreateDt();
                DistributedLock distributedLock = new DistributedLock();
                distributedLock.setName(lockConfig.getLockName());
                distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
                distributedLock.setLockedAt(now);
                distributedLock.setLockUntil(lockConfig.getLockAtMost());
                distributedLock.setCreateDt(now);
                distributedLock.setUpdateDt(now);
                return distributedLockMapper.insert(distributedLock) > 0;
            } catch (DuplicateKeyException | ConcurrencyFailureException | TransactionSystemException e) {
                return false;
            } catch (DataIntegrityViolationException | BadSqlGrammarException | UncategorizedSQLException e) {
                SnailJobLog.LOCAL.error("Unexpected exception. lockName:[{}]", lockConfig.getLockName(), e);
                return false;
            }
        }));

    }

    @Override
    public boolean renewal(LockConfig lockConfig) {
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            LocalDateTime now = lockConfig.getCreateDt();
            DistributedLock distributedLock = new DistributedLock();
            distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
            distributedLock.setLockedAt(now);
            distributedLock.setLockUntil(lockConfig.getLockAtMost());
            distributedLock.setName(lockConfig.getLockName());
            try {
                return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                        .eq(DistributedLock::getName, lockConfig.getLockName())
                        .le(DistributedLock::getLockUntil, now)) > 0;
            } catch (ConcurrencyFailureException | DataIntegrityViolationException | TransactionSystemException |
                     UncategorizedSQLException e) {
                return false;
            }
        }));
    }

    @Override
    public boolean releaseLockWithDelete(String lockName) {
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            for (int i = 0; i < 10; i++) {
                try {
                    return distributedLockMapper.delete(new LambdaUpdateWrapper<DistributedLock>()
                            .eq(DistributedLock::getName, lockName)) > 0;
                } catch (Exception e) {
                    SnailJobLog.LOCAL.error("unlock error. retrying attempt [{}] ", i, e);
                } finally {
                    CacheLockRecord.remove(lockName);
                }
            }
            return false;
        }));

    }

    @Override
    public boolean releaseLockWithUpdate(String lockName, LocalDateTime lockAtLeast) {
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(notSupportedTransaction(status -> {
            for (int i = 0; i < 10; i++) {
                try {
                    DistributedLock distributedLock = new DistributedLock();
                    distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
                    distributedLock.setLockUntil(now.isBefore(lockAtLeast) ? lockAtLeast : now);
                    return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                            .eq(DistributedLock::getName, lockName)) > 0;
                } catch (Exception e) {
                    SnailJobLog.LOCAL.error("unlock error. retrying attempt [{}] ", i, e);
                }
            }

            return false;
        }));
    }

    @Override
    public void start() {
        LockStorageFactory.registerLockStorage(this);
    }

    @Override
    public void close() {
        // 删除当前节点获取的锁记录
        distributedLockMapper.delete(new LambdaUpdateWrapper<DistributedLock>()
                .eq(DistributedLock::getLockedBy, ServerRegister.CURRENT_CID));
    }

    private Boolean notSupportedTransaction(TransactionCallback<Boolean> action) {
        TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return template.execute(action);
    }
}
