package com.aizuda.easy.retry.common.core.constant;

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
         * 批量日志上报
         */
        String BATCH_LOG_REPORT = "/batch/server/report/log";

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

    /**
     * 调度时长
     */
    Long SCHEDULE_PERIOD = 10L;

    /**
     * 延迟30s为了尽可能保障集群节点都启动完成在进行rebalance
     */
    Long SCHEDULE_INITIAL_DELAY = 30L;

    /**
     * 默认名称空间
     */
    String DEFAULT_NAMESPACE = "764d604ec6fc45f68cd92514c40e9e1a";

    /**
     * 默认Token
     */
    String DEFAULT_TOKEN = "ER_cKqBTPzCsWA3VyuCfFoccmuIEGXjr5KT";

    /**
     * AT 所有人
     */
    String AT_ALL = "all";

    /**
     * 根节点
     */
    Long ROOT = -1L;

    /**
     * 系统内置的决策任务ID
     */
    Long DECISION_JOB_ID = -1000L;

    /**
     * 系统内置的回调任务ID
     */
    Long CALLBACK_JOB_ID = -2000L;

    /**
     * 客户端返回的非json对象，单值比如 "aa", 123等
     */
    String SINGLE_PARAM = "SINGLE_PARAM";

    /**
     * 工作流触发类型
     * 仅表示定时任务类型为工作流
     */
    Integer WORKFLOW_TRIGGER_TYPE = 99;
}
