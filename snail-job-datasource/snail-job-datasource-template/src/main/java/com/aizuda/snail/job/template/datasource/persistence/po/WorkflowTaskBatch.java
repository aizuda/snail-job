package com.aizuda.snail.job.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 工作流批次
 * </p>
 *
 * @author xiaowoniu
 * @since 2023-12-12
 */
@Getter
@Setter
@TableName("workflow_task_batch")
public class WorkflowTaskBatch implements Serializable {

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
     * 工作流任务id
     */
    private Long workflowId;

    /**
     * 任务批次状态 0、失败 1、成功
     */
    private Integer taskBatchStatus;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 流程信息
     */
    private String flowInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;

    /**
     * 扩展字段
     */
    private String extAttrs;
}
