package com.aizuda.easy.retry.server.job.task.dto;

import com.aizuda.easy.retry.common.core.util.JsonUtil;
import lombok.Data;

/**
 * @author xiaowoniu
 * @date 2024-01-10 22:56:33
 * @since 2.6.0
 */
@Data
public class LogMetaDTO {

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
     * 时间
     */
    private Long timestamp;

    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
