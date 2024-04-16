package com.aizuda.snailjob.server.job.task.support.executor.workflow;

import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2023-12-24
 * @since 2.6.0
 */
@Data
public class WorkflowExecutorContext {

    private String namespaceId;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private Long workflowNodeId;

    /**
     * 工作流父节点id
     */
    private Long parentWorkflowNodeId;

    /**
     * 任务属性
     */
    private Job job;

    /**
     * 客户端返回的结果
     */
    private String taskResult;

    /**
     * 失败策略 1、跳过 2、阻塞
     */
    private Integer failStrategy;

    /**
     * 工作流节点状态 0、关闭、1、开启
     */
    private Integer workflowNodeStatus;

    /**
     * 条件节点的判定结果
     */
    private Object evaluationResult;

    /**
     * 调度任务id
     */
    private Long taskBatchId;

    /**
     * 节点信息
     */
    private String nodeInfo;

    /**
     * 任务批次状态
     */
    private Integer taskBatchStatus;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 任务状态
     */
    private Integer jobTaskStatus;

    /**
     * 日志信息
     */
    private String logMessage;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer taskExecutorScene;

    /**
     * 1、任务节点 2、条件节点 3、回调节点
     */
    private Integer nodeType;
}
