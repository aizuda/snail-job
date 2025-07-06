package com.aizuda.snailjob.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-02-22
 */
@AllArgsConstructor
@Getter
public enum RetryTaskExecutorSceneEnum {
    AUTO_RETRY(1, SyetemTaskTypeEnum.RETRY),
    MANUAL_RETRY(2, SyetemTaskTypeEnum.RETRY),
    AUTO_CALLBACK(3, SyetemTaskTypeEnum.CALLBACK),
    MANUAL_CALLBACK(4, SyetemTaskTypeEnum.CALLBACK);

    private final int scene;
    private final SyetemTaskTypeEnum taskType;
}
