package com.aizuda.easy.retry.server.common.lock.persistence;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.cache.CacheLockRecord;
import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.template.datasource.enums.DbTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.DistributedLockMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.DistributedLock;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 基于DB实现的分布式锁
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 08:34
 * @since 2.1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class JdbcLockProvider implements LockStorage, Lifecycle {

    protected static final List<String> ALLOW_DB = Arrays.asList(DbTypeEnum.MYSQL.getDb(),
        DbTypeEnum.MARIADB.getDb(),
        DbTypeEnum.POSTGRES.getDb(),
        DbTypeEnum.ORACLE.getDb());

    private final DistributedLockMapper distributedLockMapper;

    @Override
    public boolean supports(final String storageMedium) {
        return ALLOW_DB.contains(storageMedium);
    }

    @Override
    public boolean createLock(LockConfig lockConfig) {
        TransactionTemplate transactionTemplate = SpringContext.getBean(TransactionTemplate.class);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
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
                EasyRetryLog.LOCAL.error("Unexpected exception. lockName:[{}]", lockConfig.getLockName(), e);
                return false;
            }
        }));

    }

    @Override
    public boolean renewal(LockConfig lockConfig) {
        TransactionTemplate transactionTemplate = SpringContext.getBean(TransactionTemplate.class);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
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
        TransactionTemplate transactionTemplate = SpringContext.getBean(TransactionTemplate.class);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            for (int i = 0; i < 10; i++) {
                try {
                    return distributedLockMapper.delete(new LambdaUpdateWrapper<DistributedLock>()
                        .eq(DistributedLock::getName, lockName)) > 0;
                } catch (Exception e) {
                    EasyRetryLog.LOCAL.error("unlock error. retrying attempt [{}] ", i, e);
                } finally {
                    CacheLockRecord.remove(lockName);
                }
            }
            return false;
        }));
    }

    @Override
    public boolean releaseLockWithUpdate(String lockName, LocalDateTime lockAtLeast) {
        TransactionTemplate transactionTemplate = SpringContext.getBean(TransactionTemplate.class);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            for (int i = 0; i < 10; i++) {
                try {
                    DistributedLock distributedLock = new DistributedLock();
                    distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
                    distributedLock.setLockUntil(now.isBefore(lockAtLeast) ? lockAtLeast : now);
                    return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                        .eq(DistributedLock::getName, lockName)) > 0;
                } catch (Exception e) {
                    EasyRetryLog.LOCAL.error("unlock error. retrying attempt [{}] ", i, e);
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
}
