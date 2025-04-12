package com.aizuda.snailjob.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaowoniu
 * @date 2023-12-15 22:42:17
 * @since 2.6.0
 */
@Getter
@AllArgsConstructor
public enum WorkflowNodeTypeEnum {
    TASK(1, "Task node"),
    CONDITION(2, "Condition node"),
    CALLBACK(3, "Callback node");

    private final Integer nodeType;
    private final String desc;

    public static WorkflowNodeTypeEnum get(Integer nodeType) {
        for (WorkflowNodeTypeEnum workflowNodeTypeEnum : WorkflowNodeTypeEnum.values()) {
            if (workflowNodeTypeEnum.nodeType.equals(nodeType)) {
                return workflowNodeTypeEnum;
            }
        }
        return null;
    }
}
