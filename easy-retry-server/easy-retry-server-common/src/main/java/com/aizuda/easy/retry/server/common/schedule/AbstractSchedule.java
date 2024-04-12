package com.aizuda.easy.retry.server.common.schedule;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Schedule;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.lock.LockBuilder;
import com.aizuda.easy.retry.server.common.lock.LockManager;
import com.aizuda.easy.retry.server.common.lock.LockProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-07-21 13:04
 * @since 2.1.0
 */
@Slf4j
public abstract class AbstractSchedule implements Schedule {

    @Autowired
    @Qualifier("scheduledExecutorService")
    protected TaskScheduler taskScheduler;

    @Override
    public void execute() {

        String lockName = lockName();
        String lockAtMost = lockAtMost();
        String lockAtLeast = lockAtLeast();
        Assert.notBlank(lockAtMost, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockAtLeast, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockProvider lockProvider = LockBuilder.newBuilder()
                .withResident(lockName)
                .build();
        boolean lock = false;
        try {
            lock = lockProvider.lock(Duration.parse(lockAtLeast), Duration.parse(lockAtMost));
            if (lock) {
                doExecute();
            }
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error(this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        } finally {
            if (lock) {
                lockProvider.unlock();
            } else {
                LockManager.clear();
            }
        }

    }

    protected abstract void doExecute();

    protected abstract String lockName();

    protected abstract String lockAtMost();

    protected abstract String lockAtLeast();


}
