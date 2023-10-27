package com.aizuda.easy.retry.server.job.task.support.prepare;

import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.server.job.task.support.strategy.BlockStrategies;
import com.aizuda.easy.retry.server.job.task.support.strategy.BlockStrategies.BlockStrategyEnum;
import com.aizuda.easy.retry.server.job.task.support.handler.JobTaskBatchHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * 处理处于{@link JobTaskBatchStatusEnum::RUNNING}状态的任务
 *
 * @author www.byteblogs.com
 * @date 2023-10-05 18:29:22
 * @since 2.4.0
 */
@Component
@Slf4j
public class RunningJobPrepareHandler extends AbstractJobPrePareHandler {

    @Autowired
    private JobTaskBatchHandler jobTaskBatchHandler;

    @Override
    public boolean matches(Integer status) {
        return JobTaskBatchStatusEnum.RUNNING.getStatus() == status;
    }

    @Override
    protected void doHandler(JobTaskPrepareDTO prepare) {
        log.info("存在运行中的任务. taskBatchId:[{}]", prepare.getTaskBatchId());

        // 若存在所有的任务都是完成，但是批次上的状态为运行中，则是并发导致的未把批次状态变成为终态，此处做一次兜底处理
        int blockStrategy = prepare.getBlockStrategy();
        JobOperationReasonEnum jobOperationReasonEnum = JobOperationReasonEnum.NONE;
        if (jobTaskBatchHandler.complete(prepare.getTaskBatchId(), jobOperationReasonEnum.getReason())) {
            blockStrategy =  BlockStrategyEnum.CONCURRENCY.getBlockStrategy();
        } else {
            // 计算超时时间
            long delay = System.currentTimeMillis() - prepare.getExecutionAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // 计算超时时间，到达超时时间中断任务
            if (delay > prepare.getExecutorTimeout() * 1000) {
                log.info("任务执行超时.taskBatchId:[{}] delay:[{}] executorTimeout:[{}]", prepare.getTaskBatchId(), delay, prepare.getExecutorTimeout() * 1000);
                // 超时停止任务
                JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(prepare.getTaskType());
                TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(prepare);
                stopJobContext.setJobOperationReason(JobOperationReasonEnum.EXECUTE_TIMEOUT.getReason());
                stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
                instanceInterrupt.stop(stopJobContext);
            }
        }

        // 仅是超时检测的，不执行阻塞策略
        if (prepare.isOnlyTimeoutCheck()) {
            return;
        }

        BlockStrategies.BlockStrategyContext blockStrategyContext = JobTaskConverter.INSTANCE.toBlockStrategyContext(prepare);
        blockStrategyContext.setOperationReason(jobOperationReasonEnum.getReason());
        BlockStrategy blockStrategyInterface = BlockStrategies.BlockStrategyEnum.getBlockStrategy(blockStrategy);
        blockStrategyInterface.block(blockStrategyContext);

    }

}
