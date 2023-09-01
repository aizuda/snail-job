package com.aizuda.easy.retry.client.model;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 生成idempotentId模型
 *
 * @auther www.byteblogs.com
 * @date 2022/03/25 10:06
 */
@Data
public class GenerateRetryIdempotentIdDTO {
    @NotBlank(message = "group 不能为空")
    private String group;
    @NotBlank(message = "scene 不能为空")
    private String scene;
    @NotBlank(message = "参数 不能为空")
    private String argsStr;
    @NotBlank(message = "executorName 不能为空")
    private String executorName;
}
