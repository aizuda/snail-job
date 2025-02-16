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
public enum RetryOperationReasonEnum {

    NONE(0, StrUtil.EMPTY),
    TASK_EXECUTION_TIMEOUT(1, "任务执行超时"),
    NOT_CLIENT(2, "无客户端节点"),
    JOB_CLOSED(3, "JOB已关闭"),
    JOB_DISCARD(4, "任务丢弃"),
    JOB_OVERLAY(5, "任务被覆盖"),
    TASK_EXECUTION_ERROR(6, "任务执行期间发生非预期异常"),
    MANNER_STOP(7, "手动停止"),
    NOT_RUNNING_RETRY(8, "当前重试非运行中"),
    SCENE_CLOSED(9, "当前场景已关闭"),
    ;

    private final int reason;
    private final String desc;

    public static RetryOperationReasonEnum getWorkflowNotifyScene(Integer notifyScene) {
        for (RetryOperationReasonEnum sceneEnum : RetryOperationReasonEnum.values()) {
            if (sceneEnum.getReason() == notifyScene) {
                return sceneEnum;
            }
        }

        return NONE;
    }

}
