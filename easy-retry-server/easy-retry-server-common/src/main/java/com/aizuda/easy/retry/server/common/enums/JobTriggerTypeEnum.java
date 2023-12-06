package com.aizuda.easy.retry.server.common.enums;

import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * job 触发器类型枚举
 *
 * @author: xiaowoniu
 * @date : 2023-12-06 17:21
 * @since : 2.5.0
 */
@Getter
@AllArgsConstructor
public enum JobTriggerTypeEnum {
    AUTO(1, "自动触发"),
    MANUAL(2, "手动触发");

    private final Integer type;
    private final String desc;

    /**
     * 根据给定的类型获取对应的触发器类型枚举
     *
     * @param type 触发器类型的整数值
     * @return 对应的触发器类型枚举
     * @throws EasyRetryServerException 当给定的类型不是有效的枚举类型时抛出异常
     */
    public static JobTriggerTypeEnum get(Integer type) {
        for (JobTriggerTypeEnum jobTriggerTypeEnum : JobTriggerTypeEnum.values()) {
            if(jobTriggerTypeEnum.getType().equals(type)) {
                return jobTriggerTypeEnum;
            }
        }

        throw new EasyRetryServerException("无效枚举类型.[{}]", type);
    }

}
