package com.aizuda.snailjob.common.core.constant;

/**
 * 系统通用常量
 *
 * @author: opensnail
 * @date : 2022-04-17 10:47
 * @since 1.0.0
 */
public interface SystemConstants {

    /**
     * 请求头 key
     */
    String SNAIL_JOB_HEAD_KEY = "snail-job";

    /**
     * 异常重试码 key
     */
    String SNAIL_JOB_STATUS_CODE_KEY = "snail-job-status";

    /**
     * 异常重试码
     */
    String SNAIL_JOB_STATUS_CODE = "519";

    /**
     * 客户端组名
     */
    String SNAIL_JOB_CLIENT_GROUP = "group";

    /**
     * 客户端组对应token
     */
    String SNAIL_JOB_CLIENT_GROUP_TOKEN = "token";

    /**
     * 命名空间
     */
    String SNAIL_JOB_CLIENT_NAMESPACE = "namespace";

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
         * 批量上报
         */
        String BATCH_REPORT = "/batch/report";

        /**
         * 上报retry的运行结果
         */
        String REPORT_RETRY_DISPATCH_RESULT = "/report/retry/dispatch/result";

        /**
         * 上报客户端回调执行结果
         */
        String REPORT_CALLBACK_RESULT = "/report/retry/callback/result";

        /**
         * 批量日志上报
         */
        String BATCH_LOG_REPORT = "/batch/server/report/log";

        /**
         * 上报job的运行结果
         */
        String REPORT_JOB_DISPATCH_RESULT = "/report/dispatch/result";

        /**
         * 执行任务
         */
        String JOB_DISPATCH = "/job/dispatch/v1";

        /**
         * 停止任务
         */
        String JOB_STOP = "/job/stop/v1";

        /**
         * 生成同步MAP任务
         */
        String BATCH_REPORT_JOB_MAP_TASK = "/batch/report/job/map/task/v1";

        /**
         * 执行REDUCE任务
         */
        String JOB_REDUCE_TASK = "/job/reduce/task/v1";

        /**
         * 同步配置
         */
        String SYNC_CONFIG = "/sync/version";

        /**
         * 重试分发
         */
        String RETRY_DISPATCH = "/retry/dispatch/v1";

        /**
         * 重试停止
         */
        String RETRY_STOP = "/retry/stop/v1";

        /**
         * 重试回调
         */
        String RETRY_CALLBACK = "/retry/callback/v1";

        /**
         * 拉取注册的信息
         */
        String GET_REG_NODES_AND_REFRESH = "/pull/register/queue/v1";

        /**
         * 获取重试幂等id
         */
        String RETRY_GENERATE_IDEM_ID = "/retry/generate/idempotent-id/v1";

        String OPENAPI_ADD_JOB = "/api/job/add";

        String OPENAPI_UPDATE_JOB = "/api/job/update";

        String OPENAPI_GET_JOB_DETAIL = "/api/job/getJobDetail";

        String OPENAPI_GET_JOB_BATCH_DETAIL = "/api/job/getJobBatchDetail";

        String OPENAPI_GET_WORKFLOW_BATCH_DETAIL = "/api/job/getWorkflowBatchDetail";

        String OPENAPI_TRIGGER_JOB = "/api/job/triggerJob";

        String OPENAPI_TRIGGER_WORKFLOW = "/api/job/triggerWorkFlow";

        String OPENAPI_UPDATE_JOB_STATUS = "/api/job/updateJobStatus";

        String OPENAPI_UPDATE_WORKFLOW_STATUS = "/api/job/updateWorkFlowStatus";

        String OPENAPI_DELETE_JOB = "/api/job/deleteJob";

        String OPENAPI_DELETE_WORKFLOW = "/api/job/deleteWorkFlow";

    }

    String LOGO = """
              ______                  _  __       _____      __       \s
            .' ____ \\                (_)[  |     |_   _|    [  |      \s
            | (___ \\_| _ .--.  ,--.  __  | |       | | .--.  | |.--.  \s
             _.____`. [ `.-. |`'_\\ :[  | | |   _   | / .'`\\ \\| '/'`\\ \\\s
            | \\____) | | | | |// | |,| | | |  | |__' | \\__. ||  \\__/ |\s
             \\______.'[___||__]'-;__[___|___] `.____.''.__.'[__;.__.' \s
            :: Snail Job ::                                 (v{}) \s
            """;

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
    String DEFAULT_TOKEN = "SJ_Wyz3dmsdbDOkDujOTSSoBjGQP1BMsVnj";

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

    /**
     * Snail Job 认证Token
     */
    String SNAIL_JOB_AUTH_TOKEN = "SJ-TOKEN";

    /**
     * 日志类型字段
     */
    String JSON_FILED_LOG_TYPE = "logType";

    /**
     * Webhook告警、工作流回调请求密钥
     */
    String SECRET = "secret";

    /**
     * 组名、场景名、空间ID通用正则
     */
    String REGEXP = "^[A-Za-z0-9_-]{1,64}$";

    /**
     * 长时间格式
     */
    String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     *
     */
    String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * 短时间格式
     */
    String YYYY_MM_DD = "yyyy-MM-dd";


    /**
     * 动态分片的root节点
     */
    String ROOT_MAP = "ROOT_MAP";
}
