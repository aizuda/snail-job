package com.aizuda.easy.retry.common.core.constant;

import java.time.format.DateTimeFormatter;

/**
 * 系统通用常量
 *
 * @author: www.byteblogs.com
 * @date : 2022-04-17 10:47
 * @since 1.0.0
 */
public interface SystemConstants {

    /**
     * 请求头 key
     */
    String EASY_RETRY_HEAD_KEY = "easy-retry";

    /**
     * 异常重试码 key
     */
    String EASY_RETRY_STATUS_CODE_KEY = "easy-retry-status";

    /**
     * 异常重试码
     */
    String EASY_RETRY_STATUS_CODE = "519";

    /**
     * 默认的调用链超时时间 单位毫秒(ms)
     */
    long DEFAULT_DDL = 60000L;

    /**
     * 心跳
     */
    interface BEAT {
        /**
         * PING
         */
        String PING = "PING";

        /**
         * PONG
         */
        String PONG = "PONG";
    }

    /**
     * 请求路径
     */
    interface HTTP_PATH {

        /**
         * 心跳请求
         */
        String BEAT = "/beat";

        /**
         * 同步配置
         */
        String CONFIG = "/config";

        /**
         * 批量上报
         */
        String BATCH_REPORT = "/batch/report";

        /**
         * 上报job的运行结果
         */
        String REPORT_JOB_DISPATCH_RESULT = "/report/dispatch/result";
    }

    String LOGO = "  ___                ___     _            \n" +
                    " | __|__ _ ____  _  | _ \\___| |_ _ _ _  _ \n" +
                    " | _|/ _` (_-< || | |   / -_)  _| '_| || |\n" +
                    " |___\\__,_/__/\\_, | |_|_\\___|\\__|_|  \\_, |\n" +
                    "              |__/                   |__/ \n" +
                    " :: Easy Retry ::                     (v{})       \n";

    interface DATE_FORMAT {
        DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
}
