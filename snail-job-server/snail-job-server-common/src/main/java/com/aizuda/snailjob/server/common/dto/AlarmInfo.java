package com.aizuda.snailjob.server.common.dto;

import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-03 10:15:37
 * @since 2.5.0
 */
@Data
public class AlarmInfo {

    private String namespaceId;

    private String groupName;

    private Integer count;

    /**
     * 通知配置
     */
    private String notifyIds;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * 通知场景
     */
    private Integer notifyScene;

}
