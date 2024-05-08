package com.aizuda.snailjob.server.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author opensnail
 * @date 2024-05-05
 * @since sj_1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowAlarmInfo extends AlarmInfo {

    private Long id;

    /**
     * 名称
     */
    private String workflowName;

    /**
     * 任务信息id
     */
    private Long workflowId;

    /**
     * 操作原因
     */
    private Integer operationReason;

}
