package com.aizuda.snailjob.server.retry.task.support.prepare;

import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryPrePareHandler;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.generator.task.RetryTaskGeneratorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum.TERMINAL_STATUS_SET;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Component
@RequiredArgsConstructor
public class TerminalRetryPrepareHandler implements RetryPrePareHandler {
    private final RetryTaskGeneratorHandler retryTaskGeneratorHandler;

    @Override
    public boolean matches(Integer status) {
        return TERMINAL_STATUS_SET.contains(status);
    }

    @Override
    public void handle(RetryTaskPrepareDTO jobPrepareDTO) {
        retryTaskGeneratorHandler.generateRetryTask(RetryTaskConverter.INSTANCE.toRetryTaskGeneratorDTO(jobPrepareDTO));
    }
}
