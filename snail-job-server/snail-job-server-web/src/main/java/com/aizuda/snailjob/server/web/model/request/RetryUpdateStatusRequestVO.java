package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新重试任务模型
 *
 * @author opensnail
 * @date 2022-09-29
 */
@Data
public class RetryUpdateStatusRequestVO {

    /**
     * 重试状态 {@link RetryStatusEnum}
     */
    @NotBlank(message = "Retry status cannot be null")
    private Integer retryStatus;

    /**
     * 组名称
     */
    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    /**
     * 重试表id
     */
    @NotBlank(message = "Retry table ID cannot be null")
    private Long id;

}
