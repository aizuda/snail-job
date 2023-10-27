package com.aizuda.easy.retry.server.job.task.dto;

import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import lombok.Data;

/**
 * @author www.byteblogs.com
 * @date 2023-10-05 17:18:38
 * @since 2.4.0
 */
@Data
public class JobExecutorResultDTO {

    private Long jobId;

    private Long taskBatchId;

    private Long taskId;

    private String groupName;

    private Integer taskStatus;

    private String message;

    private Integer taskType;

    private Object result;

    private Integer jobOperationReason;


}
