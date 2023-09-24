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
 * 任务实例
 * </p>
 *
 * @author www.byteblogs.com
 * @since 2023-09-24
 */
@Getter
@Setter
@TableName("job_task_instance")
public class JobTaskInstance implements Serializable {

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
     * 调度任务id
     */
    private Long taskId;

    /**
     * 父执行器id
     */
    private Long parentId;

    /**
     * 执行的状态 0、失败 1、成功
     */
    private Integer executeStatus;

    /**
     * 执行结果
     */
    private String resultMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 修改时间
     */
    private LocalDateTime updateDt;


}
