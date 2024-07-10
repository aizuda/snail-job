package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1、任务节点 2、条件节点 3、回调节点
 *
 * @author xiaowoniu
 * @date 2023-12-24 08:13:43
 * @since 2.6.0
 */
@AllArgsConstructor
@Getter
public enum WorkflowNodeTypeEnum {
    JOB_TASK(1, "JOB任务"),
    DECISION(2, "决策节点"),
    CALLBACK(3, "回调节点"),
    ;

    private final int type;
    private final String desc;

    public static WorkflowNodeTypeEnum valueOf(int type) {
        for (WorkflowNodeTypeEnum workflowNodeTypeEnum : WorkflowNodeTypeEnum.values()) {
            if (workflowNodeTypeEnum.getType() == type) {
                return workflowNodeTypeEnum;
            }
        }
        return null;
    }
}
