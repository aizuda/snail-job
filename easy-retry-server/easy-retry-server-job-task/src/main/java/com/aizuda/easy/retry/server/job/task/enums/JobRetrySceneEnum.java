package com.aizuda.easy.retry.server.job.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: xiaowoniu
 * @date : 2024-02-27
 * @since : 3.1.0
 */
@AllArgsConstructor
@Getter
public enum JobRetrySceneEnum {

    AUTO(1),
    MANUAL(2);

    private final Integer retryScene;
}
