package com.aizuda.snailjob.server.web.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除重试数据
 *
 * @author: opensnail
 * @date : 2023-04-30 22:30
 */
@Data
public class BatchDeleteRetryTaskVO {

    /**
     * 组名称
     */
    @NotBlank(message = "groupName 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    /**
     * 重试表id
     */
    @NotEmpty(message = "至少选择一项")
    @Size(max = 100, message = "最多只能删除100条")
    private List<Long> ids;
}
