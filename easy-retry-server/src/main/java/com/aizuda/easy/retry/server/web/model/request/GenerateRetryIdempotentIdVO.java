package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 生成idempotentId模型
 *
 * @auther www.byteblogs.com
 * @date 2022/03/25 10:06
 */
@Data
public class GenerateRetryIdempotentIdVO {
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

    /**
     * 执行参数
     */
    @NotBlank(message = "参数不能为空")
    private String argsStr;

    /**
     * 执行器名称
     */
    @NotBlank(message = "执行器不能为空")
    private String executorName;
}
