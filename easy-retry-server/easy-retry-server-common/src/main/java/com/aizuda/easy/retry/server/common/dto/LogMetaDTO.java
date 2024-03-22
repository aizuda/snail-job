package com.aizuda.easy.retry.server.common.dto;

import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2024-01-10 22:56:33
 * @since 3.2.0
 */
@Data
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
     * 时间
     */
    private Long timestamp;

    /**
     * 日志类型
     */
    private LogTypeEnum logType;
}
