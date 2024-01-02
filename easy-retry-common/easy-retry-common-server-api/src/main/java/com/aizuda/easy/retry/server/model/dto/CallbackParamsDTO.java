package com.aizuda.easy.retry.server.model.dto;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2024-01-02
 * @since : 2.6.0
 */
@Data
public class CallbackParamsDTO {

    /**
     * 执行结果
     */
    private String resultMessage;

    /**
     * 客户端ID
     */
    private String clientInfo;

}
