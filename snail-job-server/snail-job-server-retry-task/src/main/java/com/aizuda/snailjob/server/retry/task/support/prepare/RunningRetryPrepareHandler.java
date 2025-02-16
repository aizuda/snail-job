package com.aizuda.snailjob.server.retry.task.support.prepare;

import com.aizuda.snailjob.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snailjob.common.core.enums.RetryTaskStatusEnum;
import com.aizuda.snailjob.server.retry.task.dto.RetryTaskPrepareDTO;
import com.aizuda.snailjob.server.retry.task.support.BlockStrategy;
import com.aizuda.snailjob.server.retry.task.support.RetryPrePareHandler;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.block.BlockStrategyContext;
import com.aizuda.snailjob.server.retry.task.support.block.RetryBlockStrategyFactory;
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
public class RunningRetryPrepareHandler implements RetryPrePareHandler {

    @Override
    public boolean matches(Integer status) {
        return Objects.equals(RetryTaskStatusEnum.RUNNING.getStatus(), status);
    }

    @Override
    public void handle(RetryTaskPrepareDTO prepare) {
        // 若存在所有的任务都是完成，但是批次上的状态为运行中，则是并发导致的未把批次状态变成为终态，此处做一次兜底处理
        int blockStrategy = prepare.getBlockStrategy();
        JobOperationReasonEnum jobOperationReasonEnum = JobOperationReasonEnum.NONE;
//        CompleteJobBatchDTO completeJobBatchDTO = RetryTaskConverter.INSTANCE.completeJobBatchDTO(prepare);
//        completeJobBatchDTO.setJobOperationReason(jobOperationReasonEnum.getReason());
//        if (jobTaskBatchHandler.handleResult(completeJobBatchDTO)) {
//            blockStrategy = BlockStrategyEnum.CONCURRENCY.getBlockStrategy();
//        } else {
//
//        }

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
