package com.aizuda.easy.retry.server.retry.task.support.lock;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.DistributedLockMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.DistributedLock;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;

/**
 * 基于DB实现的分布式锁
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 08:34
 * @since 2.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JdbcLockProvider extends AbstractLockProvider {

    private final DistributedLockMapper distributedLockMapper;

    @Autowired
    private SystemProperties systemProperties;

    @Override
    public boolean supports(final String storageMedium) {
        return ALLOW_DB.contains(systemProperties.getDbType().getDb());
    }

    @Override

    public boolean unlock(final LockConfig lockConfig) {
        LocalDateTime now = lockConfig.getCreateDt();
        DistributedLock distributedLock = new DistributedLock();
        distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
        distributedLock.setLockedAt(now);
        LocalDateTime lockAtLeast = lockConfig.getLockAtLeast();
        distributedLock.setLockUntil(now.isBefore(lockAtLeast) ? lockAtLeast : now);

        for (int i = 0; i < 10; i++) {
            try {
               return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                    .eq(DistributedLock::getName, lockConfig.getLockName())) > 0;
            } catch (Exception e) {
                LogUtils.error(log, "unlock error. retrying attempt [{}] ", i, e);
            }
        }

        return false;
    }

    @Override
    protected boolean insertRecord(final LockConfig lockConfig) {

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
            LogUtils.warn(log,"Duplicate key. lockName:[{}]", lockConfig.getLockName());
            return false;
        } catch (DataIntegrityViolationException | BadSqlGrammarException | UncategorizedSQLException e) {
            LogUtils.error(log,"Unexpected exception. lockName:[{}]", lockConfig.getLockName(), e);
            return false;
        }


    }

    @Override
    protected boolean updateRecord(final LockConfig lockConfig) {
        LocalDateTime now = lockConfig.getCreateDt();
        DistributedLock distributedLock = new DistributedLock();
        distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
        distributedLock.setLockedAt(now);
        distributedLock.setLockUntil(lockConfig.getLockAtMost());
        return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
            .eq(DistributedLock::getName, lockConfig.getLockName())
            .le(DistributedLock::getLockUntil, now)) > 0;
    }



}
