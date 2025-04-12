package com.aizuda.snailjob.server.common.enums;

import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * id生成模式
 *
 * @author opensnail
 * @date 2023-05-04
 * @since 2.0
 */
@AllArgsConstructor
@Getter
public enum TaskGeneratorSceneEnum {

    CLIENT_REPORT(1, "Client matching report"),
    MANA_BATCH(2, "Console manual batch addition"),
    MANA_SINGLE(3, "Console manual single addition"),
    ;

    private final int scene;

    private final String desc;

    public static TaskGeneratorSceneEnum modeOf(int scene) {
        for (TaskGeneratorSceneEnum value : TaskGeneratorSceneEnum.values()) {
            if (value.getScene() == scene) {
                return value;
            }
        }

        throw new SnailJobServerException("Unsupported task generation scenario [{}]", scene);
    }

}
