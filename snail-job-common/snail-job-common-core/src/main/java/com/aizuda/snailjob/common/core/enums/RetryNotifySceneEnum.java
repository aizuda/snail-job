package com.aizuda.snailjob.common.core.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知场景枚举
 *
 * @author: opensnail
 * @date : 2021-11-24 18:18
 * @since : 1.0.0
 */
@AllArgsConstructor
@Getter
public enum RetryNotifySceneEnum {

    NONE(0, StrUtil.EMPTY, NodeTypeEnum.SERVER),

    MAX_RETRY(1, "Scene retry count exceeds threshold", NodeTypeEnum.SERVER),

    MAX_RETRY_ERROR(2, "Scene retry failure count exceeds threshold", NodeTypeEnum.SERVER),

    CLIENT_REPORT_ERROR(3, "Client report failed", NodeTypeEnum.CLIENT),

    CLIENT_COMPONENT_ERROR(4, "Client component exception", NodeTypeEnum.CLIENT),

    RETRY_TASK_FAIL_ERROR(5, "Task retry failed", NodeTypeEnum.SERVER),

    RETRY_TASK_ENTER_DEAD_LETTER(6, "Task retry failed and entered dead letter queue", NodeTypeEnum.SERVER),

    RETRY_NO_CLIENT_NODES_ERROR(7, "Task retry failed (no executable client nodes)", NodeTypeEnum.SERVER)
    ;

    /**
     * 通知场景
     */
    private final int notifyScene;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 触发通知节点类型
     */
    private final NodeTypeEnum nodeType;

    /**
     * 获取通知场景
     *
     * @param notifyScene 场景
     * @param nodeType    触发通知节点类型
     * @return this
     */
    public static RetryNotifySceneEnum getNotifyScene(int notifyScene, NodeTypeEnum nodeType) {
        for (RetryNotifySceneEnum sceneEnum : RetryNotifySceneEnum.values()) {
            if (sceneEnum.getNotifyScene() == notifyScene && sceneEnum.nodeType.getType().equals(nodeType.getType())) {
                return sceneEnum;
            }
        }

        return null;
    }

    public static RetryNotifySceneEnum getRetryNotifyScene(Integer notifyScene) {
        for (RetryNotifySceneEnum sceneEnum : RetryNotifySceneEnum.values()) {
            if (sceneEnum.getNotifyScene() == notifyScene) {
                return sceneEnum;
            }
        }

        return NONE;
    }

}
