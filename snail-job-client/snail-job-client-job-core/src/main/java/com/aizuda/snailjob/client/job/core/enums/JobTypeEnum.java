package com.aizuda.snailjob.client.job.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JobTypeEnum {
    JOB(1),
    WORKFLOW(2);

    private final int type;
}
