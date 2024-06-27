package com.aizuda.snailjob.client.common.log.support;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.aizuda.snailjob.client.common.SnailJobLogThreadLocal;
import com.aizuda.snailjob.client.common.SnailThreadLocal;
import com.aizuda.snailjob.client.common.log.report.LogMeta;
import com.aizuda.snailjob.client.common.threadlocal.CommonThreadLocal;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;

import java.util.Objects;
import java.util.Optional;

/**
 * @author wodeyangzipingpingwuqi·
 * @date 2023-12-29
 * @since 1.0.0
 */
public final class SnailJobLogManager {

    private static final SnailThreadLocal<LogTypeEnum> LOG_TYPE = snailJobLogContextLoader();
    private static final SnailThreadLocal<LogMeta> LOG_META = snailJobLogContextLoader();

    private static <T> SnailThreadLocal<T> snailJobLogContextLoader() {
        SnailThreadLocal<T> snailThreadLocal = ServiceLoaderUtil.loadFirst(SnailJobLogThreadLocal.class);
        if (Objects.isNull(snailThreadLocal)) {
            snailThreadLocal = new CommonThreadLocal<>(new ThreadLocal<>());
        }
        return snailThreadLocal;
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

    public static void setLogType(LogTypeEnum logType) {
        LOG_TYPE.set(logType);
    }

    public static LogTypeEnum getLogType() {
        return LOG_TYPE.get();
    }

    public static void removeLogType() {
        LOG_TYPE.remove();
    }

}
