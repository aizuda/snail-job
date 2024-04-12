package com.aizuda.easy.retry.common.core.enums;

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
    TASK_EXECUTION_TIMEOUT(1, "任务执行超时"),
    NOT_CLIENT(2, "无客户端节点"),
    JOB_CLOSED(3, "JOB已关闭"),
    JOB_DISCARD(4, "任务丢弃"),
    JOB_OVERLAY(5, "任务被覆盖"),
    NOT_EXECUTION_TASK(6, "无可执行任务项"),
    TASK_EXECUTION_ERROR(7, "任务执行期间发生非预期异常"),
    MANNER_STOP(8, "手动停止"),
    WORKFLOW_CONDITION_NODE_EXECUTION_ERROR(9, "条件节点执行异常"),
    JOB_TASK_INTERRUPTED(10, "任务中断"),
    WORKFLOW_CALLBACK_NODE_EXECUTION_ERROR(11, "回调节点执行异常"),
    WORKFLOW_NODE_NO_REQUIRED(12, "无需处理"),
    WORKFLOW_NODE_CLOSED_SKIP_EXECUTION(13, "节点关闭跳过执行"),
    WORKFLOW_DECISION_FAILED(14, "判定未通过"),


    ;

    private final int reason;
    private final String desc;

    /**
     * 工作流后续节点跳过执行配置
     */
    public static final List<Integer> WORKFLOW_SUCCESSOR_SKIP_EXECUTION = Arrays.asList(
        WORKFLOW_NODE_NO_REQUIRED.getReason(), WORKFLOW_DECISION_FAILED.getReason(),
            WORKFLOW_CONDITION_NODE_EXECUTION_ERROR.getReason());

}
