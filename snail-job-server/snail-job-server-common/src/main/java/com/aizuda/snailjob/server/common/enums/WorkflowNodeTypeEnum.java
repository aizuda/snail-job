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
    TASK(1, "任务节点"),
    CONDITION(2, "条件节点"),
    CALLBACK(3, "回调节点");

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
