package com.aizuda.easy.retry.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;


/**
 * <p>
 * 调度任务
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-09-24
 */
@Getter
@Setter
@TableName("job_task_batch")
public class JobTaskBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务信息id
     */
    private Long jobId;

    /**
     * 任务批次状态
     */
    private Integer taskBatchStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 任务执行时间
     */
    private Long executionAt;

    /**
     * 操作原因
     */
    private Integer operationReason;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;

    /**
     * 逻辑删除 1、删除
     */
    private Integer deleted;


}
