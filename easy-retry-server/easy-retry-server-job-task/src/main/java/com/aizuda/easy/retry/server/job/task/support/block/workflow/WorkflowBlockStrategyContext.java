package com.aizuda.easy.retry.server.job.task.support.block.workflow;

import com.aizuda.easy.retry.server.job.task.support.block.job.BlockStrategyContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2023-12-26
 * @since : 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WorkflowBlockStrategyContext extends BlockStrategyContext  {

    /**
     * 工作流id
     */
    private Long workflowId;

    /**
     * 工作流任务批次id
     */
    private Long workflowTaskBatchId;

    /**
     * 流程信息
     */
    private String flowInfo;

}
