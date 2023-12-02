package com.aizuda.easy.retry.server.common.dto;

import lombok.Data;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2023/11/22
 */
@Data
public class JobTaskBatchReason {

    /**
     * 操作原因
     */
    private Long reason;

    /**
     * 操作原因总数
     */
    private Integer total;

}
