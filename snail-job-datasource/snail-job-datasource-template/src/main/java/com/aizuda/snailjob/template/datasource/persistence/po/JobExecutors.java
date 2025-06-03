package com.aizuda.snailjob.template.datasource.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 任务执行器
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sj_job_executors")
public class JobExecutors extends CreateUpdateDt {

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
     * 定时任务执行器名称
     */
    private String jobExecutorsName;

    /**
     * 1:java; 2:python; 3:go;
     */
    private String executorType;
}
