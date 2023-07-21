package com.aizuda.easy.retry.server.support.schedule;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.dto.LockConfig;
import com.aizuda.easy.retry.server.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.persistence.support.LockAccess;
import com.aizuda.easy.retry.server.support.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.LocalDateTime;

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
    private LockAccess lockAccess;

    @Override
    public void execute() {

        String lockName = lockName();
        String lockAtMost = lockAtMost();
        String lockAtLeast = lockAtLeast();
        Assert.notBlank(lockAtMost, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockAtLeast, () -> new EasyRetryServerException("lockAtLeast can not be null."));
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));

        LockConfig lockConfig = new LockConfig(LocalDateTime.now(), lockName, Duration.parse(lockAtMost), Duration.parse(lockAtLeast));
        try {
            if (lockAccess.lock(lockConfig)) {
                doExecute();
            }
        } catch (Exception e) {
            LogUtils.error(log, this.getClass().getName() + " execute error. lockName:[{}]", lockName, e);
        } finally {
            lockAccess.unlock(lockConfig);
        }

    }

    protected abstract void doExecute();

    abstract String lockName();

    abstract String lockAtMost();

    abstract  String lockAtLeast();

}
