package com.aizuda.easy.retry.server.web.model.response;

import com.aizuda.easy.retry.server.common.config.SystemProperties.Callback;
import com.aizuda.easy.retry.server.common.dto.CallbackConfig;
import com.aizuda.easy.retry.server.common.dto.DecisionConfig;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2023-10-12 10:18
 * @since : 2.4.0
 */
@Data
public class JobBatchResponseVO {

    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 名称
     */
    private String jobName;

    /**
     * 工作流节点名称
     */
    private String nodeName;

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 任务状态
     */
    private Integer taskBatchStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 任务执行时间
     */
    private LocalDateTime executionAt;
    /**
     * 操作原因

     */
    private Integer operationReason;

    /**
     * 执行器类型 1、Java
     */
    private Integer executorType;

    /**
     * 执行器名称
     */
    private String executorInfo;

    /**
     * 工作流的回调节点信息
     */
    private CallbackConfig callback;

    /**
     * 工作流的决策节点信息
     */
    private DecisionConfig decision;

    /**
     * 工作流批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 工作流节点id
     */
    private Long workflowNodeId;
}
