package com.aizuda.snail.job.server.retry.task.support.dispatch.task;

import java.util.HashMap;
import java.util.Map;

/**
 * @author opensnail
 * @date 2023-09-23 09:16:23
 * @since 2.4.0
 */
public class TaskActuatorFactory {

    private static final Map<TaskExecutorSceneEnum, TaskExecutor> REGISTER_TASK_ACTUATOR = new HashMap<>();

    protected static void register(TaskExecutorSceneEnum scene, TaskExecutor taskExecutor) {
        REGISTER_TASK_ACTUATOR.put(scene, taskExecutor);
    }

    public static TaskExecutor getTaskActuator(TaskExecutorSceneEnum scene) {
        return REGISTER_TASK_ACTUATOR.get(scene);
    }

}
