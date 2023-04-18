package com.aizuda.easy.retry.server.web.model.request;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2022-09-29
 * @since 2.0
 */
@Data
public class RetryTaskRequestVO {

    /**
     * 重试状态 {@link RetryStatusEnum}
     */
    private Integer retryStatus;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 重试表id
     */
    private Long id;

}
