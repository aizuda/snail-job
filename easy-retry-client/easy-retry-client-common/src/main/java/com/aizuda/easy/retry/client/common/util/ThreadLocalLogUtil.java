package com.aizuda.easy.retry.client.common.util;


import com.aizuda.easy.retry.client.common.report.LogMeta;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;

/**
 * @author wodeyangzipingpingwuqi·
 * @date 2023-12-29
 * @since 1.0.0
 */
public class ThreadLocalLogUtil {
    private static final ThreadLocal<LogTypeEnum> LOG_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<LogMeta> JOB_CONTEXT_LOCAL = new ThreadLocal<>();

    public static void initLogInfo(LogMeta logMeta, LogTypeEnum logType) {
        setContext(logMeta);
        setLogType(logType);
    }

    public static void setContext(LogMeta logMeta) {
        JOB_CONTEXT_LOCAL.set(logMeta);
    }

    public static LogMeta getContext() {
        return JOB_CONTEXT_LOCAL.get();
    }

    public static void removeContext() {
        JOB_CONTEXT_LOCAL.remove();
    }

    public static void removeAll() {
        JOB_CONTEXT_LOCAL.remove();
        LOG_TYPE.remove();
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
