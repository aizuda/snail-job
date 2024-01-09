package com.aizuda.easy.retry.server.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 日志上报DTO
 *
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-12-26
 */
@Data
public class LogTaskDTO implements Serializable {

    /**
     * 主键
     */
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
     * 创建时间
     */
    private LocalDateTime createDt;

    /**
     * 调度信息
     */
    private String message;

    /**
     * 上报时间
     */
    private Long realTime;

    /**
     * 日志模型集合
     */
    private List<TaskLogFieldDTO> fieldList;
    
}
