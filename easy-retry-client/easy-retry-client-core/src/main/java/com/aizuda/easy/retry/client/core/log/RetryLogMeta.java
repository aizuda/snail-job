package com.aizuda.easy.retry.client.core.log;

import com.aizuda.easy.retry.client.common.log.report.LogMeta;
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
