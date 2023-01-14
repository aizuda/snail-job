package com.x.retry.client.core.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
    private static String group;

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    @Data
    public static class ServerConfig {
        private String host = "127.0.0.1";
        private int port = 1788;
    }

    public static void setGroup(String group) {
        XRetryProperties.group = group;
    }

    public static String getGroup() {
        return group;
    }
}
