package com.aizuda.easy.retry.server.web.model.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:53:17
 * @since 2.6.0
 */
@Data
public class WorkflowRequestVO {

    @NotBlank(message = "组名称不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{1,64}$", message = "仅支持长度为1~64字符且类型为数字、字母和下划线")
    private String groupName;

    @NotBlank(message = "工作流名称不能为空")
    private String workflowName;

    @NotNull(message = "触发类型不能为空")
    private Integer triggerType;

    @NotBlank(message = "触发间隔不能为空")
    private String triggerInterval;

    @NotNull(message = "执行超时时间不能为空")
    private Integer executorTimeout;

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
        private String nodeName;

        /**
         * 优先级
         */
        private Integer priorityLevel;

        /**
         * 任务ID
         */
        @NotNull(message = "任务ID不能为空")
        private Long jobId;

        /**
         * 表达式类型 1、SpEl、2、Aviator 3、QL
         */
        @NotNull(message = "表达式类型不能为空")
        private Integer expressionType;

        /**
         * 条件节点表达式
         */
        private String nodeExpression;

        /**
         * 1、跳过 2、阻塞
         */
        @NotNull(message = "失败策略不能为空")
        private Integer failStrategy;

        /**
         * 工作流状态  0、关闭、1、开启
         */
        @NotNull(message = "工作流状态不能为空")
        private Integer workflowNodeStatus;

        /**
         * 子节点
         */
        private NodeConfig childNode;

    }


}
