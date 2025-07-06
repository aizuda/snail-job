package com.aizuda.snailjob.server.common.enums;

/**
 * 系统模式: 分布式重试重试、分布式定时任务
 *
 * @author opensnail
 * @date 2023-10-19 22:04:38
 * @since 2.4.0
 */
public enum SystemModeEnum {

    RETRY,
    JOB,
    ALL;

    public static boolean isRetry(SystemModeEnum mode) {
        return RETRY == mode || ALL == mode;
    }

    public static boolean isJob(SystemModeEnum mode) {
        return JOB == mode || ALL == mode;
    }
}
