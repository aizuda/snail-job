package com.aizuda.easy.retry.server.job.task.support.handler;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.LockConfig;
import com.aizuda.easy.retry.server.common.enums.UnLockOperationEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.lock.LockProvider;
import com.aizuda.easy.retry.server.job.task.support.LockExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: xiaowoniu
 * @date : 2024-01-02
 * @since : 2.6.0
 */
@Component
@Slf4j
public class DistributedLockHandler {

    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private List<LockProvider> lockProviders;

    public boolean tryLock(String lockName, String lockAtMost) {

        Assert.notBlank(lockAtMost, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockConfig lockConfig = new LockConfig(LocalDateTime.now(), lockName, Duration.parse(lockAtMost),
            Duration.ofMillis(1),
            UnLockOperationEnum.UPDATE);

        LockProvider lockProvider = getLockAccess();
        boolean lock = false;
        try {
            lock = lockProvider.lock(lockConfig);
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        }

        return lock;
    }

    public boolean unlockAndDel(String lockName) {
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockConfig lockConfig = new LockConfig(LocalDateTime.now(),
            lockName,
            Duration.ofSeconds(1), Duration.ofSeconds(0),
            UnLockOperationEnum.DELETE);

        LockProvider lockProvider = getLockAccess();
        boolean lock = false;
        try {
            lockProvider.unlock(lockConfig);
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        }

        return lock;
    }

    public boolean unlockAndUpdate(String lockName, String lockAtLeast) {

        Assert.notBlank(lockAtLeast, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockConfig lockConfig = new LockConfig(LocalDateTime.now(), lockName, Duration.ofSeconds(0),
            Duration.parse(lockAtLeast),
            UnLockOperationEnum.UPDATE);

        LockProvider lockProvider = getLockAccess();
        boolean lock = false;
        try {
            lock = lockProvider.unlock(lockConfig);
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        }

        return lock;
    }

    public void lockAndProcessAfterUnlockDel(String lockName, String lockAtMost, LockExecutor lockExecutor) {
        LockConfig lockConfig = new LockConfig(LocalDateTime.now(), lockName, Duration.parse(lockAtMost),
            Duration.ofMillis(1),
            UnLockOperationEnum.DELETE);

        LockProvider lockProvider = getLockAccess();
        boolean lock = false;
        try {
            lock = lockProvider.lock(lockConfig);
            if (lock) {
                lockExecutor.execute();
            }
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock(lockConfig);
            }
        }

    }

    public void lockAndProcessAfterUnlockUpdate(String lockName, String lockAtMost, String lockAtLeast,
        LockExecutor lockExecutor) {
        LockConfig lockConfig = new LockConfig(LocalDateTime.now(), lockName, Duration.parse(lockAtMost),
            Duration.parse(lockAtLeast),
            UnLockOperationEnum.UPDATE);

        LockProvider lockProvider = getLockAccess();
        boolean lock = false;
        try {
            lock = lockProvider.lock(lockConfig);
            if (lock) {
                lockExecutor.execute();
            }
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock(lockConfig);
            }
        }
    }

    private LockProvider getLockAccess() {
        return lockProviders.stream()
            .filter(lockProvider -> lockProvider.supports(systemProperties.getDbType().getDb()))
            .findFirst().orElseThrow(() -> new EasyRetryServerException("未找到合适锁处理器"));
    }

}
