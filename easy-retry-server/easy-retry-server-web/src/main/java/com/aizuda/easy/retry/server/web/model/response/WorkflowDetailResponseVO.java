package com.aizuda.easy.retry.server.web.model.response;

import lombok.Data;

import java.time.LocalDateTime;
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
         * 任务ID
         */
        private Long jobId;

        /**
         * 表达式类型 1、SpEl、2、Aviator 3、QL
         */
        private Integer expressionType;

        /**
         * 条件节点表达式
         */
        private String nodeExpression;

        /**
         * 1、跳过 2、阻塞
         */
        private Integer failStrategy;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        private Integer workflowNodeStatus;

        /**
         * 任务批次状态
         */
        private Integer taskBatchStatus;

        /**
         * 定时任务批次id
         */
        private Long jobTaskBatchId;

        /**
         * 任务执行时间
         */
        private LocalDateTime executionAt;

        /**
         * 操作原因
         */
        private Integer operationReason;

        /**
         * 子节点
         */
        private NodeConfig childNode;

    }


}
