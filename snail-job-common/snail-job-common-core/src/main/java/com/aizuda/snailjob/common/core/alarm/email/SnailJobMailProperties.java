package com.aizuda.snailjob.common.core.alarm.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: opensnail
 * @date : 2024-05-03
 * @since : sj_1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(value = "snail-job.mail")
public class SnailJobMailProperties implements Serializable {

    /**
     * 过滤开关
     */
    private Boolean enabled;

    /**
     * SMTP服务器域名
     */
    private String host;

    /**
     * SMTP服务端口
     */
    private Integer port;

    /**
     * 是否需要用户名密码验证
     */
    private Boolean auth;

    /**
     * 用户名
     */
    private String user;

    /**
     * 密码
     */
    private String pass;

    /**
     * 发送方，遵循RFC-822标准
     */
    private String from;

    /**
     * 使用 STARTTLS安全连接，STARTTLS是对纯文本通信协议的扩展。它将纯文本连接升级为加密连接（TLS或SSL）， 而不是使用一个单独的加密通信端口。
     */
    private Boolean starttlsEnable;

    /**
     * 使用 SSL安全连接
     */
    private Boolean sslEnable;

    /**
     * SMTP超时时长，单位毫秒，缺省值不超时
     */
    private Long timeout;

    /**
     * Socket连接超时值，单位毫秒，缺省值不超时
     */
    private Long connectionTimeout;

    /**
     * 额外的会话属性
     * eg: mail.smtp.ssl.trust
     */
    private Map<String,String> properties;

}
