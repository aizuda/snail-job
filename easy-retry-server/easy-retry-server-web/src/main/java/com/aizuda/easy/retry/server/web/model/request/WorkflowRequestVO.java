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

    @NotBlank(message = "触发类型不能为空")
    private Integer triggerType;

    @NotBlank(message = "触发间隔不能为空")
    private String triggerInterval;

    @NotNull(message = "执行超时时间不能为空")
    private Integer executorTimeout;

    @NotEmpty(message = "执行超时时间不能为空")
    @Valid
    private List<NodeInfo> nodeInfos;

    @Data
    public static class NodeInfo {

        /**
         * 条件节点表达式
         */
        private String nodeExpression;

        @NotNull(message = "节点类型不能为空")
        private Integer nodeType;

        @NotNull(message = "任务ID不能为空")
        private Long jobId;

        @NotNull(message = "表达式类型不能为空")
        private Integer expressionType;

        @NotNull(message = "失败策略不能为空")
        private Integer failStrategy;

        /**
         * 子节点
         */
        private List<NodeInfo> childrenList;

    }


}
