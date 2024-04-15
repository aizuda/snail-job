package com.aizuda.snail.job.server.common.enums;

import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
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

    CLIENT_REPORT(1,"客户端匹配上报"),
    MANA_BATCH(2, "控制台手动批量新增"),
    MANA_SINGLE(3, "控制台手动单个新增"),
    ;

    private final int scene;

    private final String desc;

    public static TaskGeneratorSceneEnum modeOf(int scene) {
        for (TaskGeneratorSceneEnum value : TaskGeneratorSceneEnum.values()) {
            if (value.getScene() == scene) {
                return value;
            }
        }

       throw new EasyRetryServerException("不支持的任务生成场景 [{}]", scene);
    }

}
