package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;

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
