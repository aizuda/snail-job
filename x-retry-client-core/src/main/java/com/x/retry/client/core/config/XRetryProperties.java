package com.x.retry.client.core.config;

import com.x.retry.common.core.context.SpringContext;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-04 15:53
 */
@Configuration
@ConfigurationProperties(prefix = "x-retry")
@Getter
@Setter
public class XRetryProperties {

    /**
     * 服务端对应的group
     */
    private String group;

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

    public static String getGroup() {
        XRetryProperties properties = SpringContext.applicationContext.getBean(XRetryProperties.class);
        return Objects.requireNonNull(properties).group;
    }
}
