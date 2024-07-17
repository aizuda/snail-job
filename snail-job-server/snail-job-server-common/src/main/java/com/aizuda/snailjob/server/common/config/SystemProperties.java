package com.aizuda.snailjob.server.common.config;

import com.aizuda.snailjob.common.core.alarm.email.SnailJobMailProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 系统配置
 *
 * @author: opensnail
 * @date : 2021-12-21 10:19
 */
@Configuration
@ConfigurationProperties(value = "snail-job")
@Data
public class SystemProperties {

    /**
     * 重试每次拉取的条数
     */
    private int retryPullPageSize = 1000;

    /**
     * 任务调度每次拉取的条数
     */
    private int jobPullPageSize = 1000;

    /**
     * 重试每次拉取的次数
     */
    private int retryMaxPullCount = 10;

    /**
     * netty 端口
     */
    private int nettyPort = 1788;

    /**
     * 一个客户端每秒最多接收的重试数量指令
     */
    private int limiter = 100;

    /**
     * 号段模式下步长配置 默认100
     */
    private int step = 100;

    /**
     * 日志默认保存天数
     */
    private int logStorage = 90;

    /**
     * 合并日志默认保存天数
     */
    private int mergeLogDays = 1;

    /**
     * 合并日志默认的条数
     */
    private int mergeLogNum = 500;

    /**
     * 负载均衡周期时间
     */
    private int loadBalanceCycleTime = 10;

    /**
     * 桶的总数量
     */
    private int bucketTotal = 128;

    /**
     * 回调配置
     */
    private Callback callback = new Callback();

    /**
     * 回调配置
     */
    @Data
    public static class Callback {

        /**
         * 回调uniqueId前缀
         */
        String prefix = "CB";

        /**
         * 回调的最大执行次数
         */
        private int maxCount = 288;

        /**
         * 间隔时间
         */
        private long triggerInterval = 15 * 60;

    }

    /**
     * Dashboard 任务容错天数
     */
    private int summaryDay = 7;

    /**
     * 邮件配置
     */
    @NestedConfigurationProperty
    private SnailJobMailProperties mail = new SnailJobMailProperties();

}
