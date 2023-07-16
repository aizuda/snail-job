package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * 解析参数模型
 *
 * @author: www.byteblogs.com
 * @date: 2023-07-15 23:15
 */
@Data
public class ParseLogsVO {

    /**
     * 客户端打印的上报日志信息
     */
    @NotBlank(message = "日志信息不能为空")
    private String logStr;

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    private String groupName;

    /**
     * 场景名称
     */
    @NotBlank(message = "场景名称不能为空")
    private String sceneName;

    @NotNull(message = "重试状态不能为空")
    private Integer retryStatus;
}
