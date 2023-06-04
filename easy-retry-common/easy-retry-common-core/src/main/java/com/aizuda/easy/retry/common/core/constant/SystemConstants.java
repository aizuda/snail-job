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

    }

    interface CALL_BACK {
        /**
         * 回调id前缀
         */
        String CB_ = "CB_";
        /**
         * 最大重试次数
         */
        int MAX_RETRY_COUNT = 288;
        /**
         * 间隔时间
         */
        int TRIGGER_INTERVAL = 15 * 60;
    }

}
