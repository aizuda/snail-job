package com.aizuda.easy.retry.server.job.task.support.block.job;

import lombok.Data;

/**
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@Data
public class BlockStrategyContext {

    private Long jobId;

    private Long taskBatchId;

    private String namespaceId;

    private String groupName;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 下次触发时间
     */
    private Long nextTriggerAt;

    private Integer operationReason;

    /**
     * 执行策略 1、auto 2、manual 3、workflow
     */
    private Integer taskExecutorScene;

}
