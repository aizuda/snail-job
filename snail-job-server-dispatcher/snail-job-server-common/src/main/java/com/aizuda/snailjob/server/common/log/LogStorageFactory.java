package com.aizuda.snailjob.server.common.log;

import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.aizuda.snailjob.server.common.LogStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: xiaowoniu
 * @date : 2024-03-22
 * @since : 3.2.0
 */
public final class LogStorageFactory {

    private LogStorageFactory() {
    }

    private static final Map<LogTypeEnum, LogStorage> LOG_STORAGE = new ConcurrentHashMap<>();

    public static void register(LogTypeEnum logType, LogStorage logStorage) {
        LOG_STORAGE.put(logType, logStorage);
    }

    public static LogStorage get(LogTypeEnum logType) {
        return LOG_STORAGE.get(logType);
    }
}
