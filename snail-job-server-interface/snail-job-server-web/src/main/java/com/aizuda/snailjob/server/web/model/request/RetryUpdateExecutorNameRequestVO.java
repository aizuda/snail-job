package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 更新执行器名称
 *
 * @author opensnail
 * @date 2022-09-29
 */
@Data
public class RetryUpdateExecutorNameRequestVO {

    /**
     * 组名称
     */
    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    /**
     * 执行器名称
     */
    private String executorName;

    /**
     * 重试状态 {@link RetryStatusEnum}
     */
    private Integer retryStatus;

    /**
     * 重试表id
     */
    @NotEmpty(message = "At least one item must be selected")
    private List<Long> ids;

}
