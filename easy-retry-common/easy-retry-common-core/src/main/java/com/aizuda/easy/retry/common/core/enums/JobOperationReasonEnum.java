package com.aizuda.easy.retry.common.core.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标识某个操作的具体原因
 *
 * @author www.byteblogs.com
 * @date 2023-10-07 23:05:50
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum JobOperationReasonEnum {

    NONE(0, StrUtil.EMPTY),
    EXECUTE_TIMEOUT(1, "执行超时"),
    NOT_CLIENT(2, "无客户端节点"),
    JOB_CLOSED(3, "任务已关闭"),
    ;

    private final int reason;
    private final String desc;

}
