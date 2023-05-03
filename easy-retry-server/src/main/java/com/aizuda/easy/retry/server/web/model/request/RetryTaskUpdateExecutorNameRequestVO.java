package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

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
    @NotBlank(message = "groupName 不能为空")
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
