package com.aizuda.snailjob.common.core.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 标识某个操作的具体原因
 *
 * @author opensnail
 * @date 2023-10-07 23:05:50
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum JobOperationReasonEnum {

    NONE(0, StrUtil.EMPTY),
    TASK_EXECUTION_TIMEOUT(1, "Task execution timeout"),
    NOT_CLIENT(2, "No client nodes"),
    JOB_CLOSED(3, "JOB closed"),
    JOB_DISCARD(4, "Task discarded"),
    JOB_OVERLAY(5, "Task overridden"),
    NOT_EXECUTION_TASK(6, "No executable task items"),
    TASK_EXECUTION_ERROR(7, "Unexpected exception occurred during task execution"),
    MANNER_STOP(8, "Manual stop"),
    WORKFLOW_CONDITION_NODE_EXECUTION_ERROR(9, "Condition node execution exception"),
    JOB_TASK_INTERRUPTED(10, "Task interrupted"),
    WORKFLOW_CALLBACK_NODE_EXECUTION_ERROR(11, "Callback node execution exception"),
    WORKFLOW_NODE_NO_REQUIRED(12, "No action required"),
    WORKFLOW_NODE_CLOSED_SKIP_EXECUTION(13, "Node closed, skipped execution"),
    WORKFLOW_DECISION_FAILED(14, "Judgment not passed"),


    ;

    private final int reason;
    private final String desc;

    /**
     * 工作流后续节点跳过执行配置
     */
    public static final List<Integer> WORKFLOW_SUCCESSOR_SKIP_EXECUTION = Arrays.asList(
            WORKFLOW_NODE_NO_REQUIRED.getReason(), WORKFLOW_DECISION_FAILED.getReason(),
            WORKFLOW_CONDITION_NODE_EXECUTION_ERROR.getReason());

    public static JobOperationReasonEnum getWorkflowNotifyScene(Integer notifyScene) {
        for (JobOperationReasonEnum sceneEnum : JobOperationReasonEnum.values()) {
            if (sceneEnum.getReason() == notifyScene) {
                return sceneEnum;
            }
        }

        return NONE;
    }

}
