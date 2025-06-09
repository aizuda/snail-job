package com.aizuda.snailjob.server.common.config;

import com.aizuda.snailjob.common.core.alarm.email.SnailJobMailProperties;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import com.aizuda.snailjob.common.core.util.NetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
     * 重试任务拉取的并行度
     */
    private int retryMaxPullParallel = 2;

    /**
     * 任务调度每次拉取的条数
     */
    private int jobPullPageSize = 1000;

    /**
     * netty 端口
     * see: serverPort
     */
    @Deprecated
    private Integer nettyPort;

    /**
     * 服务端端口
     */
    private int serverPort = 17888;

    /**
     * 获取服务端端口
     */
    public int getServerPort() {
        // since: 1.3.0-beta1 兼容nettyPort
        return Objects.isNull(nettyPort) ? serverPort : nettyPort;
    }

    /**
     * 服务端地址
     */
    private String serverHost = NetUtil.getLocalIpStr();

    /**
     * server token
     */
    private String serverToken = "SJ_H9HGGmrX3QBVTfsAAG2mcKH3SR7bCLsK";

    /**
     * 单个节点支持的最大调度量
     */
    private int maxDispatchCapacity = 10000;

    /**
     * 日志默认保存天数
     */
    private int logStorage = 7;

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
     * Dashboard 任务容错天数
     */
    private int summaryDay = 7;

    /**
     * rpc类型
     */
    private RpcTypeEnum rpcType = RpcTypeEnum.GRPC;

    /**
     * 邮件配置
     */
    @NestedConfigurationProperty
    private SnailJobMailProperties mail = new SnailJobMailProperties();

    /**
     * 客户端Rpc配置
     */
    private RpcClientProperties clientRpc = new RpcClientProperties();

    /**
     * 服务端Rpc配置
     */
    private RpcServerProperties serverRpc = new RpcServerProperties();

    @Data
    public static class RpcServerProperties {

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private Duration keepAliveTime = Duration.of(30, ChronoUnit.SECONDS);

        private Duration keepAliveTimeout = Duration.of(10, ChronoUnit.SECONDS);

        private Duration permitKeepAliveTime = Duration.of(5, ChronoUnit.MINUTES);

        private ThreadPoolConfig dispatcherTp = new ThreadPoolConfig(16, 16, 1, TimeUnit.SECONDS, 10000);

    }

    @Data
    public static class RpcClientProperties {

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private Duration keepAliveTime = Duration.of(30, ChronoUnit.SECONDS);

        private Duration keepAliveTimeout = Duration.of(10, ChronoUnit.SECONDS);

        private Duration idleTimeout = Duration.of(5, ChronoUnit.MINUTES);

        private ThreadPoolConfig clientTp = new ThreadPoolConfig(16, 16, 1, TimeUnit.SECONDS, 10000);

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ThreadPoolConfig {

        /**
         * 核心线程池
         */
        private int corePoolSize = 16;

        /**
         * 最大线程数
         */
        private int maximumPoolSize = 16;

        /**
         * 线程存活时间
         */
        private long keepAliveTime = 1;

        /**
         * 线程存活时间(单位)
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;

        /**
         * 队列容量
         */
        private int queueCapacity = 10000;
    }
}
