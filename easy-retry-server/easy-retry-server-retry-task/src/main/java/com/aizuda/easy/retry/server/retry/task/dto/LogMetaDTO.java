package com.aizuda.easy.retry.server.retry.task.dto;

import com.aizuda.easy.retry.common.core.util.JsonUtil;
import lombok.Builder;
import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2024-01-10 22:56:33
 * @since 3.2.0
 */
@Data
@Builder
public class LogMetaDTO {

    /**
     * 命名空间
     */
    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 组名称
     */
    private String uniqueId;

    /**
     * 时间
     */
    private Long timestamp;

    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
