package com.aizuda.easy.retry.common.core.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标识某个操作的具体原因
 *
 * @author www.byteblogs.com
 * @date 2023-10-07 23:05:50
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum JobOperationReasonEnum {

    NONE(0, StrUtil.EMPTY),
    EXECUTE_TIMEOUT(1, "任务执行超时"),
    NOT_CLIENT(2, "无客户端节点"),
    JOB_CLOSED(3, "JOB已关闭"),
    JOB_DISCARD(4, "任务丢弃"),
    JOB_OVERLAY(5, "任务被覆盖"),
    NOT_EXECUTE_TASK(6, "无可执行任务项"),
    TASK_EXECUTE_ERROR(7, "任务执行期间发生非预期异常"),
    MANNER_STOP(8, "手动停止"),
    WORKFLOW_CONDITION_NODE_EXECUTOR_ERROR(8, "条件节点执行异常"),
    JOB_TASK_INTERRUPTED(8, "任务中断"),
    ;

    private final int reason;
    private final String desc;

}
