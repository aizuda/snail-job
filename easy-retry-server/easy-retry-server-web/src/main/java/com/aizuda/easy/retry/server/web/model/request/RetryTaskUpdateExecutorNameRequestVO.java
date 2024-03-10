package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * 更新执行器名称
 *
 * @author www.byteblogs.com
 * @date 2022-09-29
 */
@Data
public class RetryTaskUpdateExecutorNameRequestVO {

    /**
     * 组名称
     */
    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
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
    @NotEmpty(message = "至少选择一项")
    private List<Long> ids;

}
