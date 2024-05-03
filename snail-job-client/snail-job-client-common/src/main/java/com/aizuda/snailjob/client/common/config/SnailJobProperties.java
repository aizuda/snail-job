package com.aizuda.snailjob.client.common.config;

import com.aizuda.snailjob.common.core.alarm.email.MailProperties;
import com.aizuda.snailjob.common.core.context.SpringContext;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
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
    private int port = 1789;

    /**
     * 重试、调度日志远程上报滑动窗口配置
     */
    private LogSlidingWindowConfig logSlidingWindow = new LogSlidingWindowConfig();

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    /**
     * 调度线程池配置
     */
    private DispatcherThreadPool dispatcherThreadPool = new DispatcherThreadPool();

    /**
     * 重试模块配置
     */
    private Retry retry = new Retry();

    @Data
    public static class ServerConfig {
        /**
         * 服务端的地址，若服务端集群部署则此处配置域名
         */
        private String host = "127.0.0.1";

        /**
         * 服务端netty的端口号
         */
        private int port = 1788;
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
    public static class DispatcherThreadPool  {

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
        private long keepAliveTime;

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
    public static class Retry {
        /**
         * 远程上报滑动窗口配置
         */
        private SlidingWindowConfig reportSlidingWindow = new SlidingWindowConfig();
    }

    public static String getGroup() {
        SnailJobProperties properties = SpringContext.getBean(SnailJobProperties.class);
        return Objects.requireNonNull(properties).group;
    }

    /**
     * 邮件配置
     */
    private MailProperties mail = new MailProperties();
}
