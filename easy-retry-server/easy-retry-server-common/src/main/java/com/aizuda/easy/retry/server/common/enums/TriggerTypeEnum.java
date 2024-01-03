package com.aizuda.easy.retry.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 1 CRON表达式 2 固定时间 3 工作流
 * @author xiaowoniu
 * @date 2024-01-03 22:10:01
 * @since 2.6.0
 */
@AllArgsConstructor
@Getter
public enum TriggerTypeEnum {
    CRON(1, "CRON表达式"),
    FIXED_TIME(2, "固定时间"),
    WORKFLOW(3, "工作流");

    private final Integer type;
    private final String desc;

    public static TriggerTypeEnum get(Integer type) {
        for (TriggerTypeEnum triggerTypeEnum : TriggerTypeEnum.values()) {
            if (triggerTypeEnum.type.equals(type)) {
                return triggerTypeEnum;
            }
        }
        return null;
    }
}
