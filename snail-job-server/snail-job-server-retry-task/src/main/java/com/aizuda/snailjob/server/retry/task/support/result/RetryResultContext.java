package com.aizuda.snailjob.server.retry.task.support.result;

import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.retry.task.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RetryResultContext extends BaseDTO {

//    /**
//     * 客户端返回的结果
//     * @see RetryResultStatusEnum
//     */
//    private Integer resultStatus;

    /**
     * 重试任务状态
     * @see RetryTaskStatusEnum
     */
    private Integer taskStatus;
    private Integer operationReason;

    private boolean incrementRetryCount;
    private String resultJson;
    private String idempotentId;
    private String exceptionMsg;

}
