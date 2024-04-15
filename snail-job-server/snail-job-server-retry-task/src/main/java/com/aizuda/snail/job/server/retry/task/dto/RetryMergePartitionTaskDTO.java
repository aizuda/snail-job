package com.aizuda.snail.job.server.retry.task.dto;

import com.aizuda.snail.job.server.common.dto.PartitionTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2024-03-27
 * @since : 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryMergePartitionTaskDTO extends PartitionTask {

}
