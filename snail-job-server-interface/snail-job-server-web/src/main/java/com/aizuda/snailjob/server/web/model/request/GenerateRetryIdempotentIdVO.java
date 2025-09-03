package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成idempotentId模型
 *
 * @auther opensnail
 * @date 2022/03/25 10:06
 */
@Data
public class GenerateRetryIdempotentIdVO {
    /**
     * 组名称
     */
    @NotBlank(message = "Group name cannot be null")
    private String groupName;

    /**
     * 场景名称
     */
    @NotBlank(message = "Scene name cannot be null")
    private String sceneName;

    /**
     * 执行参数
     */
    @NotBlank(message = "Parameters cannot be null")
    private String argsStr;

    /**
     * 执行器名称
     */
    @NotBlank(message = "Executor cannot be null")
    private String executorName;

    /**
     * 参数序列化器名称
     */
    @NotBlank(message = "serializerName cannot be null")
    private String serializerName;
}
