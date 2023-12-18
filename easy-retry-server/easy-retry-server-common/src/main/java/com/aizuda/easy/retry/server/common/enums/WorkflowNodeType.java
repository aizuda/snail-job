package com.aizuda.easy.retry.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xiaowoniu
 * @date 2023-12-15 22:42:17
 * @since 2.6.0
 */
@Getter
@AllArgsConstructor
public enum WorkflowNodeType {
    TASK(1, "任务节点"),
    Condition(1, "条件节点"),
    callback(1, "回调节点");

    private final Integer nodeType;
    private final String desc;

    public static WorkflowNodeType get(Integer nodeType) {
        for (WorkflowNodeType workflowNodeType : WorkflowNodeType.values()) {
            if (workflowNodeType.nodeType.equals(nodeType)) {
                return workflowNodeType;
            }
        }
        return null;
    }
}
