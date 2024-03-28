package com.aizuda.easy.retry.client.common.config;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * easy retry 客户端配置
 *
 * @author: www.byteblogs.com
 * @date : 2022-03-04 15:53
 * @since 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "easy-retry")
@Getter
@Setter
public class EasyRetryProperties {

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
     * 指定客户端IP，默认取本地IP
     */
    private String host;

    /**
     * 指定客户端端口
     */
    private Integer port;

    /**
     * 远程上报滑动窗口配置
     */
    private SlidingWindowConfig slidingWindow = new SlidingWindowConfig();

    /**
     * 重试、调度日志远程上报滑动窗口配置
     */
    private LogSlidingWindowConfig logSlidingWindow = new LogSlidingWindowConfig();

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

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

    public static String getGroup() {
        EasyRetryProperties properties = SpringContext.getBean(EasyRetryProperties.class);
        return Objects.requireNonNull(properties).group;
    }
}
