package com.aizuda.snail.job.server.common.dto;

import lombok.Data;

/**
 * 回调节点配置
 *
 * @author xiaowoniu
 * @date 2023-12-30 11:18:14
 * @since 2.6.0
 */
@Data
public class CallbackConfig {

    /**
     * webhook
     */
    private String webhook;

    /**
     * 请求类型
     */
    private Integer contentType;

    /**
     * 秘钥
     */
    private String secret;
}
