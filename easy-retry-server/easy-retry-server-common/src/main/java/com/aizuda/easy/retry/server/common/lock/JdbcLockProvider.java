package com.aizuda.easy.retry.server.common.lock;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.cache.CacheLockRecord;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.server.common.enums.UnLockOperationEnum;
import com.aizuda.easy.retry.server.common.register.ServerRegister;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.DistributedLockMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.DistributedLock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
public class JdbcLockProvider extends AbstractLockProvider implements Lifecycle {

    private final DistributedLockMapper distributedLockMapper;

    @Autowired
    private SystemProperties systemProperties;

    @Override
    public boolean supports(final String storageMedium) {
        return ALLOW_DB.contains(systemProperties.getDbType().getDb());
    }

    @Override
    public boolean unlock(final LockConfig lockConfig) {
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 10; i++) {
            try {
                if (lockConfig.getUnLockOperation() == UnLockOperationEnum.UPDATE) {
                    DistributedLock distributedLock = new DistributedLock();
                    distributedLock.setLockedBy(ServerRegister.CURRENT_CID);
                    LocalDateTime lockAtLeast = lockConfig.getLockAtLeast();
                    distributedLock.setLockUntil(now.isBefore(lockAtLeast) ? lockAtLeast : now);
                    return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                        .eq(DistributedLock::getName, lockConfig.getLockName())) > 0;
                } else {
                    CacheLockRecord.remove(lockConfig.getLockName());
                    return distributedLockMapper.delete(new LambdaUpdateWrapper<DistributedLock>()
                        .eq(DistributedLock::getName, lockConfig.getLockName())) > 0;
                }
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
            return false;
        } catch (DataIntegrityViolationException | BadSqlGrammarException | UncategorizedSQLException e) {
            LogUtils.error(log, "Unexpected exception. lockName:[{}]", lockConfig.getLockName(), e);
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
        distributedLock.setName(lockConfig.getLockName());
        try {
            return distributedLockMapper.update(distributedLock, new LambdaUpdateWrapper<DistributedLock>()
                .eq(DistributedLock::getName, lockConfig.getLockName())
                .le(DistributedLock::getLockUntil, now)) > 0;
        } catch (ConcurrencyFailureException | DataIntegrityViolationException | TransactionSystemException |
                 UncategorizedSQLException e) {
            return false;
        }

    }

    @Override
    public void start() {
    }

    @Override
    public void close() {
        // 删除当前节点获取的锁记录
        distributedLockMapper.delete(new LambdaUpdateWrapper<DistributedLock>()
            .eq(DistributedLock::getLockedBy, ServerRegister.CURRENT_CID));
    }
}
