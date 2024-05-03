package com.aizuda.snailjob.client.common.log.support;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.snailjob.client.common.SnailLogContext;
import com.aizuda.snailjob.client.common.log.context.ThreadLocalLogContext;
import com.aizuda.snailjob.client.common.log.report.LogMeta;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;

import java.util.Optional;

/**
 * @author wodeyangzipingpingwuqi·
 * @date 2023-12-29
 * @since 1.0.0
 */
public final class SnailJobLogManager {

    private static final SnailLogContext<LogTypeEnum> LOG_TYPE = snailJobLogContextLoader();
    private static final SnailLogContext<LogMeta> LOG_META = snailJobLogContextLoader();

    private static <T> SnailLogContext<T> snailJobLogContextLoader() {
        return Optional.ofNullable(ServiceLoaderUtil.loadFirst(SnailLogContext.class)).orElse(new ThreadLocalLogContext<T>(new ThreadLocal<>()));
    }

    private SnailJobLogManager() {
    }

    public static void initLogInfo(LogMeta logMeta, LogTypeEnum logType) {
        setLogMeta(logMeta);
        setLogType(logType);
    }

    public static void setLogMeta(LogMeta logMeta) {
        LOG_META.set(logMeta);
    }

    public static LogMeta getLogMeta() {
        return LOG_META.get();
    }

    public static void removeLogMeta() {
        LOG_META.remove();
    }

    public static void removeAll() {
        removeLogMeta();
        removeLogType();
    }

    public static void setLogType (LogTypeEnum logType) {
        LOG_TYPE.set(logType);
    }

    public static LogTypeEnum getLogType () {
        return LOG_TYPE.get();
    }

    public static void removeLogType () {
        LOG_TYPE.remove();
    }

}
