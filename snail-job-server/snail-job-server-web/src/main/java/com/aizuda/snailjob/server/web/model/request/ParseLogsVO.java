package com.aizuda.snailjob.server.web.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 解析参数模型
 *
 * @author: opensnail
 * @date: 2023-07-15 23:15
 */
@Data
public class ParseLogsVO {

    /**
     * 客户端打印的上报日志信息
     */
    @NotBlank(message = "组名称不能为空")
    private String logStr;

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotNull(message = "重试状态不能为空")
    private Integer retryStatus;
}
