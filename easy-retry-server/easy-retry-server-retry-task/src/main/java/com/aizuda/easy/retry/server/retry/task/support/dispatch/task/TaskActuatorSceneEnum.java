package com.aizuda.easy.retry.server.retry.task.support.dispatch.task;

import com.aizuda.easy.retry.server.common.enums.TaskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author www.byteblogs.com
 * @date 2023-09-23 08:49:21
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum TaskActuatorSceneEnum {
    AUTO_RETRY(1, TaskTypeEnum.RETRY),
    MANUAL_RETRY(2,  TaskTypeEnum.RETRY),
    AUTO_CALLBACK(3, TaskTypeEnum.CALLBACK),
    MANUAL_CALLBACK(4, TaskTypeEnum.CALLBACK);

    private final int scene;
    private final TaskTypeEnum taskType;


}
