package com.aizuda.easy.retry.server.support.generator.task;

import com.aizuda.easy.retry.server.enums.TaskGeneratorScene;
import org.springframework.stereotype.Component;

/**
 * 控制台手动单个新增
 *
 * @author www.byteblogs.com
 * @date 2023-07-16 11:51:56
 * @since 2.1.0
 */
@Component
public class ManaSingleRetryGenerator extends AbstractGenerator {
    @Override
    public boolean supports(int scene) {
        return TaskGeneratorScene.MANA_SINGLE.getScene() == scene;
    }
}
