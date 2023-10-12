package com.aizuda.easy.retry.server.job.task.support.generator.batch;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 13:12:48
 * @since 2.4.0
 */
@Data
public class JobTaskBatchGeneratorContext {

    /**
     * 组名称
     */
    private String groupName;

    /**
     * 任务id
     */
    private Long jobId;

    /**
     * 任务类型
     */
    private Integer taskInstanceType;

    /**
     * 下次触发时间
     */
    private LocalDateTime nextTriggerAt;


}
