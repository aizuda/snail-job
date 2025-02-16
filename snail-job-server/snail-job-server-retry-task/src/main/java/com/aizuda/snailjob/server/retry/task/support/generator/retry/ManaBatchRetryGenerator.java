package com.aizuda.snailjob.server.retry.task.support.generator.retry;

import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.server.common.enums.TaskGeneratorSceneEnum;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 控制台手动批量新增
 *
 * @author opensnail
 * @date 2023-07-16 11:51:56
 * @since 2.1.0
 */
@Component
public class ManaBatchRetryGenerator extends AbstractGenerator {
    @Override
    public boolean supports(int scene) {
        return TaskGeneratorSceneEnum.MANA_BATCH.getScene() == scene;
    }

    @Override
    protected Integer initStatus(TaskContext taskContext) {
        return Optional.ofNullable(taskContext.getInitStatus()).orElse(RetryStatusEnum.RUNNING.getStatus());
    }
}
