package com.aizuda.easy.retry.server.job.task.dto;

import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import com.aizuda.easy.retry.server.common.dto.LogMetaDTO;
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
