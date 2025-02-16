package com.aizuda.snailjob.server.retry.task.support.block;

import com.aizuda.snailjob.common.core.enums.JobBlockStrategyEnum;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.generator.task.RetryTaskGeneratorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class ConcurrencyRetryBlockStrategy extends AbstracJobBlockStrategy {
    private final RetryTaskGeneratorHandler retryTaskGeneratorHandler;

    @Override
    public void doBlock(final BlockStrategyContext context) {
        // 重新生成任务
        RetryTaskGeneratorDTO generatorDTO = RetryTaskConverter.INSTANCE.toRetryTaskGeneratorDTO(context);
        retryTaskGeneratorHandler.generateRetryTask(generatorDTO);
    }

    @Override
    protected JobBlockStrategyEnum blockStrategyEnum() {
        return JobBlockStrategyEnum.CONCURRENCY;
    }
}
