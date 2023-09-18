package com.aizuda.easy.retry.server.common.dto;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2023-07-21 08:43
 * @since 2.1.0
 */
public class LockConfig {

    private final LocalDateTime createDt;

    private final String lockName;

    private final Duration lockAtMost;

    private final Duration lockAtLeast;

    public LockConfig(final LocalDateTime createDt, final String lockName, final Duration lockAtMost, final Duration lockAtLeast) {
        this.lockName = lockName;
        this.lockAtMost = lockAtMost;
        this.lockAtLeast = lockAtLeast;
        this.createDt = createDt;
        Assert.notNull(createDt, () -> new EasyRetryServerException("createDt can not be null."));
        Assert.notBlank(lockName, () -> new EasyRetryServerException("lockName can not be null."));
        Assert.notNull(lockAtMost, () -> new EasyRetryServerException("lockAtMost can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtMost.isNegative(), () -> new EasyRetryServerException("lockAtMost  is negative. lockName:[{}]", lockName));
        Assert.notNull(lockAtLeast, () -> new EasyRetryServerException("lockAtLeast can not be null. lockName:[{}]", lockName));
        Assert.isFalse(lockAtLeast.compareTo(lockAtMost) > 0, () -> new EasyRetryServerException("lockAtLeast is longer than lockAtMost for lock. lockName:[{}]", lockName));
    }

    public LocalDateTime getCreateDt() {
        return createDt;
    }

    public String getLockName() {
        return lockName;
    }

    public LocalDateTime getLockAtMost() {
        return createDt.plus(lockAtMost);
    }

    public LocalDateTime getLockAtLeast() {
        return createDt.plus(lockAtLeast);
    }
}
