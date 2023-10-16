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
 * 调度日志
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-09-24
 */
@Getter
@Setter
@TableName("job_log_message")
public class JobLogMessage implements Serializable {

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
     * 任务实例id
     */
    private Long taskBatchId;

    /**
     * 调度任务id
     */
    private Long taskId;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 调度信息
     */
    private String message;


}
