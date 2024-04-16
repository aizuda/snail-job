package com.aizuda.snailjob.common.log;

import com.aizuda.snailjob.common.log.strategy.Local;
import com.aizuda.snailjob.common.log.strategy.Remote;
import com.aizuda.snailjob.common.log.strategy.Local;
import com.aizuda.snailjob.common.log.strategy.Remote;

/**
 * 静态日志类，用于在不引入日志对象的情况下打印日志
 *
 * @author wodeyangzipingpingwuqi
 */
public final class SnailJobLog {
    private SnailJobLog() {}

    public static final Local LOCAL = new Local();

    public static final Remote REMOTE = new Remote();
}
