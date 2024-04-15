package com.aizuda.snail.job.server.retry.task.generator.task;

import com.aizuda.snail.job.common.core.enums.RetryStatusEnum;
import com.aizuda.snail.job.server.common.enums.TaskGeneratorSceneEnum;
import org.springframework.stereotype.Component;

/**
 * 客户端上报任务生成器
 *
 * @author opensnail
 * @date 2023-07-16 11:51:56
 * @since 2.1.0
 */
@Component
public class ClientReportRetryGenerator extends AbstractGenerator {
    @Override
    public boolean supports(int scene) {
        return TaskGeneratorSceneEnum.CLIENT_REPORT.getScene() == scene;
    }

    @Override
    protected Integer initStatus(TaskContext taskContext) {
        return RetryStatusEnum.RUNNING.getStatus();
    }
}
