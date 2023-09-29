package com.aizuda.easy.retry.server.job.task.enums;

import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 10:39:22
 * @since 2.4.0
 */
@AllArgsConstructor
@Getter
public enum TaskTypeEnum {

    CLUSTER(1),
    BROADCAST(2),
    SHARDING(3);

    private final int type;

    public static TaskTypeEnum valueOf(int type) {
        for (TaskTypeEnum value : TaskTypeEnum.values()) {
            if (value.getType() == type) {
                return value;
            }
        }

        throw new EasyRetryServerException("未知类型");
    }
}
