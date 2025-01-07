package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调度日志
 *
 * @author opensnail
 * @since 2023-09-24
 */
@Data
@TableName("sj_job_log_message")
@EqualsAndHashCode(callSuper=true)
public class JobLogMessage extends CreateDt {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 命名空间
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
     * 任务实例id
     */
    private Long taskBatchId;

    /**
     * 调度任务id
     */
    private Long taskId;

    /**
     * 日志数量
     */
    private Integer logNum;

    /**
     * 调度信息
     */
    private String message;

    /**
     * 真实上报时间
     */
    private Long realTime;

}
