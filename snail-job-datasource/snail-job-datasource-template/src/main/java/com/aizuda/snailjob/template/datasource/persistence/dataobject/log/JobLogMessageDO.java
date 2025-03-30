package com.aizuda.snailjob.template.datasource.persistence.dataobject.log;

import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-03-30
 */
@Data
public class JobLogMessageDO {

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
