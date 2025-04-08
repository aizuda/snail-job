package com.aizuda.snailjob.server.retry.task.support.generator.retry;

import lombok.Data;

import java.util.List;

/**
 * 任务生成器上下文
 *
 * @author opensnail
 * @date 2023-07-16 21:26:52
 * @since 2.1.0
 */
@Data
public class TaskContext {

    /**
     * namespaceId
     */
    private String namespaceId;

    /**
     * groupName
     */
    private String groupName;

    /**
     * groupId
     */
    private Long groupId;

    /**
     * sceneName
     */
    private String sceneName;

    /**
     * sceneId
     */
    private Long sceneId;

    /**
     * 任务的初始状态
     */
    private Integer initStatus;

    /**
     * 是否初始化场景
     */
    private Integer initScene;

    /**
     * 任务信息
     */
    private List<TaskInfo> taskInfos;

    @Data
    public static class TaskInfo {
        /**
         * 业务唯一id
         */
        private String idempotentId;

        /**
         * 执行器名称
         */
        private String executorName;

        /**
         * 业务唯一编号
         */
        private String bizNo;

        /**
         * 客户端上报参数
         */
        private String argsStr;

        /**
         * 额外扩展参数
         */
        private String extAttrs;
    }
}
