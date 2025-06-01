package com.aizuda.snailjob.server.common.vo.request;

import com.aizuda.snailjob.server.common.dto.CallbackConfig;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.common.dto.JobTaskConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:53:17
 * @since 2.6.0
 */
@Data
public class WorkflowRequestVO {

    private Long id;

    @NotBlank(message = "Group name cannot be null")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "Only supports 1~64 characters, including numbers, letters, underscores, and hyphens")
    private String groupName;

    @NotBlank(message = "Workflow name cannot be empty")
    private String workflowName;

    @NotNull(message = "Trigger type cannot be empty")
    private Integer triggerType;

    @NotBlank(message = "Trigger interval cannot be empty")
    private String triggerInterval;

    @NotNull(message = "Execution timeout cannot be empty")
    private Integer executorTimeout;

    @NotNull(message = "Blocking strategy cannot be null")
    private Integer blockStrategy;

    /**
     * 工作流上下文
     */
    private String wfContext;

    /**
     * 0、关闭、1、开启
     */
    @NotNull(message = "Workflow status")
    private Integer workflowStatus;

    /**
     * 描述
     */
    private String description;

    /**
     * DAG节点配置
     */
    @NotNull(message = "DAG node configuration cannot be empty")
    private NodeConfig nodeConfig;

    @Data
    public static class NodeConfig {

        /**
         * 1、任务节点 2、条件节点
         */
        @NotNull(message = " Node type cannot be empty")
        private Integer nodeType;

        /**
         * 节点信息
         */
        @NotEmpty(message = "Node information cannot be empty")
        private List<NodeInfo> conditionNodes;

        /**
         * 子节点
         */
        private NodeConfig childNode;

    }

    @Data
    public static class NodeInfo {

        /**
         * 节点名称
         */
        @NotBlank(message = "Node name cannot be empty")
        private String nodeName;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        @NotNull(message = "Workflow status cannot be empty")
        private Integer workflowNodeStatus;

        /**
         * 优先级
         */
        @NotNull(message = "Priority cannot be empty")
        private Integer priorityLevel;

        /**
         * 子节点
         */
        private NodeConfig childNode;

        /**
         * 1、跳过 2、阻塞
         */
        private Integer failStrategy;

        /**
         * 任务节点配置
         */
        private JobTaskConfig jobTask;

        /**
         * 决策节点配置
         */
        private DecisionConfig decision;

        /**
         * 回调配置
         */
        private CallbackConfig callback;
    }

    /**
     * 通知告警场景配置id列表
     */
    private Set<Long> notifyIds;

    /**
     * 负责人id
     */
    private Long ownerId;
}
