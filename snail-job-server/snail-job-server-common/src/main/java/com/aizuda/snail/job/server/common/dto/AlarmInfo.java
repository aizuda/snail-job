package com.aizuda.snail.job.server.common.dto;

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
}
