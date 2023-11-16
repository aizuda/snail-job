package com.aizuda.easy.retry.server.support.schedule;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.config.SystemProperties;
import com.aizuda.easy.retry.server.dto.LockConfig;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.support.Schedule;
import com.aizuda.easy.retry.server.support.lock.LockProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-07-21 13:04
 * @since 2.1.0
 */
@Slf4j
public abstract class AbstractSchedule implements Schedule {

    @Autowired
    @Qualifier("scheduledExecutorService")
    protected TaskScheduler taskScheduler;
    @Autowired
    private List<LockProvider> lockProviders;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public void execute() {

        String lockName = lockName();
        String lockAtMost = lockAtMost();
        String lockAtLeast = lockAtLeast();
        Assert.notBlank(lockAtMost, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockAtLeast, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockConfig lockConfig = new LockConfig(LocalDateTime.now(), lockName, Duration.parse(lockAtMost), Duration.parse(lockAtLeast));

        LockProvider lockProvider = getLockAccess();
        boolean lock = false;
        try {
            lock = lockProvider.lock(lockConfig);
            if (lock) {
                doExecute();
            }
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock(lockConfig);
            }
        }


    }

    protected abstract void doExecute();

    abstract String lockName();

    abstract String lockAtMost();

    abstract String lockAtLeast();

    private LockProvider getLockAccess() {
        return lockProviders.stream()
                .filter(lockProvider -> lockProvider.supports(systemProperties.getDbType().getDb()))
                .findFirst().orElseThrow(() -> new EasyRetryServerException("未找到合适锁处理器"));
    }

}
