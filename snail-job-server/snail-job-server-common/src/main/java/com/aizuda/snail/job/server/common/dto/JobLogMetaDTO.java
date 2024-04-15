package com.aizuda.snail.job.server.common.dto;

import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.common.log.enums.LogTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author xiaowoniu
 * @date 2024-01-10 22:56:33
 * @since 2.6.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JobLogMetaDTO extends LogMetaDTO {

    public JobLogMetaDTO () {
        setLogType(LogTypeEnum.JOB);
    }

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


    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
