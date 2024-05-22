package com.aizuda.snailjob.server.web.model.request;

import com.aizuda.snailjob.server.common.dto.CallbackConfig;
import com.aizuda.snailjob.server.common.dto.DecisionConfig;
import com.aizuda.snailjob.server.common.dto.JobTaskConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:53:17
 * @since 2.6.0
 */
@Data
public class WorkflowRequestVO {

    private Long id;

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母、下划线和短横线")
    private String groupName;

    @NotBlank(message = "工作流名称不能为空")
    private String workflowName;

    @NotNull(message = "触发类型不能为空")
    private Integer triggerType;

    @NotBlank(message = "触发间隔不能为空")
    private String triggerInterval;

    @NotNull(message = "执行超时时间不能为空")
    private Integer executorTimeout;

    @NotNull(message = "阻塞策略不能为空")
    private Integer blockStrategy;

    /**
     * 0、关闭、1、开启
     */
    @NotNull(message = "工作流状态")
    private Integer workflowStatus;

    /**
     * 描述
     */
    private String description;

    /**
     * DAG节点配置
     */
    @NotNull(message = "DAG节点配置不能为空")
    private NodeConfig nodeConfig;

    @Data
    public static class NodeConfig {

        /**
         * 1、任务节点 2、条件节点
         */
        @NotNull(message = "节点类型不能为空 ")
        private Integer nodeType;

        /**
         * 节点信息
         */
        @NotEmpty(message = "节点信息不能为空")
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
        @NotBlank(message = "节点名称不能为空")
        private String nodeName;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        @NotNull(message = "工作流状态不能为空")
        private Integer workflowNodeStatus;

        /**
         * 优先级
         */
        @NotNull(message = "优先级不能为空")
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

}
