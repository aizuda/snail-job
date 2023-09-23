package com.aizuda.easy.retry.server.retry.task.support.dispatch.task;

import com.aizuda.easy.retry.template.datasource.access.Access;

import java.util.HashMap;
import java.util.Map;

/**
 * @author www.byteblogs.com
 * @date 2023-09-23 09:16:23
 * @since 2.4.0
 */
public class TaskActuatorFactory {

    private static final Map<TaskActuatorSceneEnum, TaskActuator> REGISTER_TASK_ACTUATOR = new HashMap<>();

    protected static void register(TaskActuatorSceneEnum scene,TaskActuator taskActuator) {
        REGISTER_TASK_ACTUATOR.put(scene, taskActuator);
    }

    public static TaskActuator getTaskActuator(TaskActuatorSceneEnum scene) {
        return REGISTER_TASK_ACTUATOR.get(scene);
    }

}
