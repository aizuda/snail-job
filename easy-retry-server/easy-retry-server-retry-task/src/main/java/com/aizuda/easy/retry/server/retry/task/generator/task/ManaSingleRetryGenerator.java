package com.aizuda.easy.retry.server.retry.task.generator.task;

import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.server.common.enums.TaskGeneratorSceneEnum;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
        return TaskGeneratorSceneEnum.MANA_SINGLE.getScene() == scene;
    }

    @Override
    protected Integer initStatus(TaskContext taskContext) {
        return Optional.ofNullable(taskContext.getInitStatus()).orElse(RetryStatusEnum.RUNNING.getStatus());
    }
}
