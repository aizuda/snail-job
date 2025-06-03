package com.aizuda.snailjob.server.model.dto;

import lombok.Data;

/**
 * @author zhouxuangang
 * @date 2025/6/3 23:11
 */
@Data
public class JobExecutorDTO {
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
