package com.aizuda.easy.retry.server.support.generator.task;

import lombok.Data;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-07-16 21:26:52
 * @since
 */
@Data
public class TaskContext {

    /**
     * 加密的groupId
     */
    private String groupName;

    /**
     * 加密的sceneId
     */
    private String sceneName;

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
