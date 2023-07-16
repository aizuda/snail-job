package com.aizuda.easy.retry.server.support.generator.task;

import com.aizuda.easy.retry.server.enums.TaskGeneratorScene;
import org.springframework.stereotype.Component;

/**
 * 客户端上报任务生成器
 *
 * @author www.byteblogs.com
 * @date 2023-07-16 11:51:56
 * @since 2.1.0
 */
@Component
public class ClientReportRetryGenerator extends AbstractGenerator {
    @Override
    public boolean supports(int scene) {
        return TaskGeneratorScene.CLIENT_REPORT.getScene() == scene;
    }
}
