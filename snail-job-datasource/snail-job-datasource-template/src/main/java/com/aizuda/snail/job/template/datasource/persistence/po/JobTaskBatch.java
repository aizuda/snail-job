package com.aizuda.snail.job.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 调度任务
 *
 * @author opensnail
 * @since 2023-09-24
 */
@Data
@TableName("sj_job_task_batch")
public class JobTaskBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 命名空间id
     */
    private String namespaceId;


    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 工作流批次id
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
     * 任务批次状态
     */
    private Integer taskBatchStatus;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 任务类型 3、JOB任务 4、WORKFLOW任务
     */
    private Integer systemTaskType;


    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

}
