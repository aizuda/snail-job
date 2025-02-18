package com.aizuda.snailjob.server.retry.task.support.block;

import com.aizuda.snailjob.common.core.enums.JobBlockStrategyEnum;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskGeneratorDTO;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.generator.task.RetryTaskGeneratorHandler;
import com.aizuda.snailjob.server.retry.task.support.handler.RetryTaskStopHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2025-02-10
 * @since : sj_1.4.0
 */
@Component
@RequiredArgsConstructor
public class OverlayRetryBlockStrategy extends AbstracJobBlockStrategy {
    private final RetryTaskGeneratorHandler retryTaskGeneratorHandler;
    private final RetryTaskStopHandler retryTaskStopHandler;

    @Override
    public void doBlock(final BlockStrategyContext context) {

        // 重新生成任务
        RetryTaskGeneratorDTO generatorDTO = RetryTaskConverter.INSTANCE.toRetryTaskGeneratorDTO(context);
        generatorDTO.setTaskStatus(RetryTaskStatusEnum.CANCEL.getStatus());
        generatorDTO.setOperationReason(RetryOperationReasonEnum.JOB_DISCARD.getReason());
        retryTaskGeneratorHandler.generateRetryTask(generatorDTO);

        TaskStopJobDTO stopJobDTO = RetryTaskConverter.INSTANCE.toTaskStopJobDTO(context);
        if (Objects.isNull(context.getOperationReason()) || context.getOperationReason() == JobOperationReasonEnum.NONE.getReason()) {
            stopJobDTO.setOperationReason(RetryOperationReasonEnum.JOB_OVERLAY.getReason());
        }

        stopJobDTO.setNeedUpdateTaskStatus(true);
        retryTaskStopHandler.stop(stopJobDTO);

    }

    @Override
    protected JobBlockStrategyEnum blockStrategyEnum() {
        return JobBlockStrategyEnum.OVERLAY;
    }
}
