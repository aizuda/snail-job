package com.aizuda.easy.retry.server.support.generator.task;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.enums.TaskGeneratorScene;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 控制台手动批量新增
 *
 * @author www.byteblogs.com
 * @date 2023-07-16 11:51:56
 * @since 2.1.0
 */
@Component
public class ManaBatchRetryGenerator extends AbstractGenerator {
    @Override
    public boolean supports(int scene) {
        return TaskGeneratorScene.MANA_BATCH.getScene() == scene;
    }

    @Override
    protected Integer initStatus(TaskContext taskContext) {
        return Optional.ofNullable(taskContext.getInitStatus()).orElse(RetryStatusEnum.RUNNING.getStatus());
    }
}
