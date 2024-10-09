package com.aizuda.snailjob.client.job.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TriggerTypeEnum {
    SCHEDULED_TIME(2),
    CRON(3),
    WORK_FLOW(99);

    private final int type;
}
