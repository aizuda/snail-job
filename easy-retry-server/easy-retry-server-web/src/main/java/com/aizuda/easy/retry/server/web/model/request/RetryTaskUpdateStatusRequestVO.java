package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 更新重试任务模型
 *
 * @author www.byteblogs.com
 * @date 2022-09-29
 */
@Data
public class RetryTaskUpdateStatusRequestVO {

    /**
     * 重试状态 {@link RetryStatusEnum}
     */
    @NotBlank(message = "重试状态 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private Integer retryStatus;

    /**
     * 组名称
     */
    @NotBlank(message = "组名称 不能为空")
    private String groupName;

    /**
     * 重试表id
     */
    @NotBlank(message = "重试表id 不能为空")
    private Long id;

}
