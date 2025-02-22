package com.aizuda.snailjob.common.core.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标识某个操作的具体原因
 *
 * @author opensnail
 * @date 2023-10-07 23:05:50
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum RetryOperationReasonEnum {

    NONE(0, StrUtil.EMPTY),
    TASK_EXECUTION_TIMEOUT(1, "任务执行超时"),
    NOT_CLIENT(2, "无客户端节点"),
    RETRY_SUSPEND(3, "重试已暂停"),
    RETRY_TASK_DISCARD(4, "任务丢弃"),
    RETRY_TASK_OVERLAY(5, "任务被覆盖"),
    TASK_EXECUTION_ERROR(6, "任务执行期间发生非预期异常"),
    MANNER_STOP(7, "手动停止"),
    NOT_RUNNING_RETRY(8, "当前重试非运行中"),
    SCENE_CLOSED(9, "当前场景已关闭"),
    RETRY_FAIL(10, "重试失败"),
    CLIENT_TRIGGER_RETRY_STOP(11, "客户端触发任务停止"),
    ;

    private final int reason;
    private final String desc;

    public static RetryOperationReasonEnum of(Integer operationReason) {
        if (operationReason == null) {
            return NONE;
        }

        for (RetryOperationReasonEnum sceneEnum : RetryOperationReasonEnum.values()) {
            if (sceneEnum.getReason() == operationReason) {
                return sceneEnum;
            }
        }

        return NONE;
    }

}
