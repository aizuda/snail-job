package com.aizuda.snailjob.server.retry.task.support.prepare;

import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.dto.TaskStopJobDTO;
import com.aizuda.snailjob.server.retry.task.support.BlockStrategy;
import com.aizuda.snailjob.server.retry.task.support.RetryPrePareHandler;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.block.BlockStrategyContext;
import com.aizuda.snailjob.server.retry.task.support.block.RetryBlockStrategyFactory;
import com.aizuda.snailjob.server.retry.task.support.handler.RetryTaskStopHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-01-26
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RunningRetryPrepareHandler implements RetryPrePareHandler {
    private final RetryTaskStopHandler retryTaskStopHandler;

    @Override
    public boolean matches(Integer status) {
        return Objects.equals(RetryTaskStatusEnum.RUNNING.getStatus(), status);
    }

    @Override
    public void handle(RetryTaskPrepareDTO prepare) {
        // 若存在所有的任务都是完成，但是批次上的状态为运行中，则是并发导致的未把批次状态变成为终态，此处做一次兜底处理
        int blockStrategy = prepare.getBlockStrategy();
        JobOperationReasonEnum jobOperationReasonEnum = JobOperationReasonEnum.NONE;

        // 计算超时时间
        long delay = DateUtils.toNowMilli() - prepare.getNextTriggerAt();

        // 计算超时时间，到达超时时间中断任务
        if (delay > DateUtils.toEpochMilli(prepare.getExecutorTimeout())) {
            log.info("任务执行超时.retryTaskId:[{}] delay:[{}] executorTimeout:[{}]", prepare.getRetryTaskId(), delay, DateUtils.toEpochMilli(prepare.getExecutorTimeout()));
            // 超时停止任务
            TaskStopJobDTO stopJobDTO = RetryTaskConverter.INSTANCE.toTaskStopJobDTO(prepare);
            stopJobDTO.setOperationReason(JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());
            stopJobDTO.setNeedUpdateTaskStatus(true);
            retryTaskStopHandler.stop(stopJobDTO);
        }

        // 仅是超时检测的，不执行阻塞策略
        if (prepare.isOnlyTimeoutCheck()) {
            return;
        }

        BlockStrategyContext blockStrategyContext = RetryTaskConverter.INSTANCE.toBlockStrategyContext(prepare);
        blockStrategyContext.setOperationReason(jobOperationReasonEnum.getReason());
        BlockStrategy blockStrategyInterface = RetryBlockStrategyFactory.getBlockStrategy(blockStrategy);
        blockStrategyInterface.block(blockStrategyContext);

    }
}
