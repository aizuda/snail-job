package com.aizuda.snail.job.client.core.log;

import com.aizuda.snail.job.client.common.log.report.LogMeta;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: xiaowoniu
 * @date : 2024-03-21
 * @since : 3.2.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryLogMeta extends LogMeta {

    private String uniqueId;

}
