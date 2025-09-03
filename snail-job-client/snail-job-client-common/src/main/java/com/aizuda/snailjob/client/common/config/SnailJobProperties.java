package com.aizuda.snailjob.client.common.config;

import com.aizuda.snailjob.common.core.alarm.email.SnailJobMailProperties;
import com.aizuda.snailjob.common.core.config.ForyProperties;
import com.aizuda.snailjob.common.core.enums.RpcTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * snail job 客户端配置
 *
 * @author: opensnail
 * @date : 2022-03-04 15:53
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "snail-job")
@Getter
@Setter
public class SnailJobProperties {

    /**
     * 命名空间ID
     * 若不填则默认为 SystemConstants::DEFAULT_NAMESPACE
     */
    private String namespace;

    /**
     * 服务端对应的group
     */
    private String group;

    /**
     * 令牌
     * 若不填则默认为 SystemConstants::DEFAULT_TOKEN
     */
    private String token;

    /**
     * 指定客户端IP，默认取本地IP
     */
    private String host;

    /**
     * 指定客户端端口
     */
    private Integer port = 17889;

    /**
     * rpc类型
     */
    private RpcTypeEnum rpcType = RpcTypeEnum.GRPC;

    /**
     * 标签
     */
    private Map<String, String> labels = new HashMap<>();

    /**
     * 重试、调度日志远程上报滑动窗口配置
     */
    private LogSlidingWindowConfig logSlidingWindow = new LogSlidingWindowConfig();

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    /**
     * 内置http执行器自定义响应结果配置
     */
    private HttpResponse httpResponse;

    /**
     * 重试模块配置
     */
    private Retry retry = new Retry();

    /**
     * 邮件配置
     */
    @NestedConfigurationProperty
    private SnailJobMailProperties mail = new SnailJobMailProperties();

    /**
     * Fory 配置
     */
    @NestedConfigurationProperty
    private ForyProperties fory = new ForyProperties();

    /**
     * openapi配置
     */
    public SnailOpenApiConfig openapi = new SnailOpenApiConfig();

    /**
     * 客户端脚本存储位置
     */
    private String workspace;

    /**
     * 客户端Rpc配置
     */
    private RpcClientProperties clientRpc = new RpcClientProperties();

    /**
     * 服务端Rpc配置
     */
    private RpcServerProperties serverRpc = new RpcServerProperties();

    @Data
    public static class ServerConfig {
        /**
         * 服务端的地址，若服务端集群部署则此处配置域名
         */
        private String host = "127.0.0.1";

        /**
         * 服务端 rpc 的端口号
         */
        private int port = 17888;
    }

    @Data
    public static class HttpResponse {
        /**
         * 内置http执行器响应成功状态码，默认值200
         */
        private Integer code = 200;

        /**
         * 内置http执行器状态码字段名称，默认值code，只针对responseType等于json生效
         */
        private String field = "code";

        /**
         * 内置http执行器响应类型，可选值json或者text，默认值json
         */
        private String responseType = "json";
    }

    @Data
    public static class SlidingWindowConfig {

        /**
         * 总量窗口期阈值
         */
        private int totalThreshold = 50;

        /**
         * 窗口数量预警
         */
        private int windowTotalThreshold = 150;

        /**
         * 窗口期时间长度
         */
        private long duration = 10;

        /**
         * 窗口期单位
         */
        private ChronoUnit chronoUnit = ChronoUnit.SECONDS;

    }

    @Data
    public static class LogSlidingWindowConfig {

        /**
         * 总量窗口期阈值
         */
        private int totalThreshold = 50;

        /**
         * 窗口数量预警
         */
        private int windowTotalThreshold = 150;

        /**
         * 窗口期时间长度
         */
        private long duration = 5;

        /**
         * 窗口期单位
         */
        private ChronoUnit chronoUnit = ChronoUnit.SECONDS;

    }

    @Data
    public static class Retry {
        /**
         * 远程上报滑动窗口配置
         */
        private SlidingWindowConfig reportSlidingWindow = new SlidingWindowConfig();

        /**
         * 本地执行重试或者回调配置
         */
        private ThreadPoolConfig dispatcherThreadPool = new ThreadPoolConfig(32, 32, 1, TimeUnit.SECONDS , 10000);
    }

    @Data
    public static class RpcServerProperties {

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private Duration keepAliveTime = Duration.of(2, ChronoUnit.HOURS);

        private Duration keepAliveTimeout = Duration.of(20, ChronoUnit.SECONDS);

        private Duration permitKeepAliveTime = Duration.of(5, ChronoUnit.MINUTES);

        private ThreadPoolConfig dispatcherTp = new ThreadPoolConfig(16, 16, 1, TimeUnit.SECONDS , 10000);

    }

    @Data
    public static class RpcClientProperties {

        private int maxInboundMessageSize = 10 * 1024 * 1024;

        private Duration keepAliveTime = Duration.of(30, ChronoUnit.SECONDS);

        private Duration keepAliveTimeout = Duration.of(10, ChronoUnit.SECONDS);

        private Duration permitKeepAliveTime = Duration.of(5, ChronoUnit.MINUTES);

        private Duration idleTimeout = Duration.of(5, ChronoUnit.MINUTES);

        private ThreadPoolConfig clientTp = new ThreadPoolConfig(16, 16, 1, TimeUnit.SECONDS , 10000);

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

    @Data
    public static final class SnailOpenApiConfig {
        /**
         * 默认可以不配置，不配置则取{@link SnailJobProperties.ServerConfig#getHost()}
         */
        private String host;
        /**
         * 默认是8080
         */
        private int port = 8080;
        /**
         * 是否是https协议
         */
        private boolean https;
        /**
         * 公共前缀
         */
        private String prefix = "snail-job";
    }

}
