package com.aizuda.snailjob.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @since 1.5.0
 */
@Getter
@AllArgsConstructor
public enum WebSocketSceneEnum {
    JOB_LOG_SCENE(1, "JOB_LOG_SCENE"),
    WORKFLOW_LOG_SCENE(2, "WORKFLOW_LOG_SCENE"),
    RETRY_LOG_SCENE(3,"RETRY_LOG_SCENE");

    private final Integer type;
    private final String scene;

}
