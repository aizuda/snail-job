package com.aizuda.snail.job.server.model.dto;

import lombok.Data;

/**
 * 工作流回调节点参数模型
 *
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
