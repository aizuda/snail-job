package com.aizuda.snail.job.server.web.model.response;

import com.aizuda.snail.job.server.common.dto.CallbackConfig;
import com.aizuda.snail.job.server.common.dto.DecisionConfig;
import com.aizuda.snail.job.server.common.dto.JobTaskConfig;
import lombok.Data;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-14 22:59:33
 * @since 2.6.0
 */
@Data
public class WorkflowDetailResponseVO {

    /**
     * 工作流ID
     */
    private Long id;

    /**
     * 组名称
     */
    private String workflowName;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 触发类型
     */
    private Integer triggerType;

    /**
     * 阻塞策略
     */
    private Integer blockStrategy;

    /**
     * 触发间隔
     */
    private String triggerInterval;

    /**
     * 超时时间
     */
    private Integer executorTimeout;

    /**
     * 0、关闭、1、开启
     */
    private Integer workflowStatus;

    /**
     * DAG节点配置
     */
    private NodeConfig nodeConfig;

    @Data
    public static class NodeConfig {

        /**
         * 1、任务节点 2、条件节点 3、回调节点
         */
        private Integer nodeType;

        /**
         * 节点信息
         */
        private List<NodeInfo> conditionNodes;

        /**
         * 子节点
         */
        private NodeConfig childNode;

    }

    @Data
    public static class NodeInfo {

        /**
         * 节点ID
         */
        private Long id;

        /**
         * 1、任务节点 2、条件节点 3、回调节点
         */
        private Integer nodeType;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 优先级
         */
        private Integer priorityLevel;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        private Integer workflowNodeStatus;

        /**
         * 失败策略 1、跳过 2、阻塞
         */
        private Integer failStrategy;

        /**
         * 任务批次状态
         */
        private Integer taskBatchStatus;

        /**
         * 判定配置
         */
        private DecisionConfig decision;

        /**
         * 回调配置
         */
        private CallbackConfig callback;

        /**
         * 任务配置
         */
        private JobTaskConfig jobTask;

        /**
         * 定时任务批次信息
         */
        private List<JobBatchResponseVO> jobBatchList;

        /**
         * 子节点
         */
        private NodeConfig childNode;

    }

}
