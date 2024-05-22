package com.aizuda.snailjob.server.common.schedule;

import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Schedule;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.lock.LockBuilder;
import com.aizuda.snailjob.server.common.lock.LockManager;
import com.aizuda.snailjob.server.common.lock.LockProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;

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
        Assert.notBlank(lockAtMost, () -> new SnailJobServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockAtLeast, () -> new SnailJobServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> new SnailJobServerException("lockName can not be null."));

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
            SnailJobLog.LOCAL.error(this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
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
