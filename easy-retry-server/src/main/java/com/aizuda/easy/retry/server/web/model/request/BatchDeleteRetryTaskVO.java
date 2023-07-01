package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

/**
 * 批量删除重试数据
 *
 * @author: www.byteblogs.com
 * @date : 2023-04-30 22:30
 */
@Data
public class BatchDeleteRetryTaskVO {

    /**
     * 组名称
     */
    @NotBlank(message = "groupName 不能为空")
    private String groupName;

    /**
     * 重试表id
     */
    @NotEmpty(message = "至少选择一项")
    private List<Long> ids;
}
