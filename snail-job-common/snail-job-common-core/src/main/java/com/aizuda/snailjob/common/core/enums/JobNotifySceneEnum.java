package com.aizuda.snailjob.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知场景枚举
 *
 * @author: zuoJunLin
 * @date : 2023-12-02 18:18
 */
@AllArgsConstructor
@Getter
public enum JobNotifySceneEnum {

    /********************************Job****************************************/
    JOB_TASK_ERROR(1, "JOB任务执行失败", NodeTypeEnum.SERVER),
    JOB_CLIENT_ERROR(2, "客户端执行失败", NodeTypeEnum.CLIENT),

    /********************************Workflow****************************************/
    WORKFLOW_TASK_ERROR(100, "Workflow任务执行失败", NodeTypeEnum.SERVER);

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
    public static JobNotifySceneEnum getJobNotifyScene(int notifyScene, NodeTypeEnum nodeType) {
        for (JobNotifySceneEnum sceneEnum : JobNotifySceneEnum.values()) {
            if (sceneEnum.getNotifyScene() == notifyScene && sceneEnum.nodeType.getType().equals(nodeType.getType())) {
                return sceneEnum;
            }
        }

        return null;
    }


}
