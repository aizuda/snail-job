package com.x.retry.client.model;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 服务端调度重试入参
 *
 * @auther www.byteblogs.com
 * @date 2022/03/25 10:06
 */
@Data
public class DispatchRetryDTO {
    @NotBlank(message = "scene 不能为空")
    private String scene;
    @NotBlank(message = "参数 不能为空")
    private String argsStr;
    @NotBlank(message = "bizId 不能为空")
    private String bizId;
    @NotBlank(message = "executorName 不能为空")
    private String executorName;
}
