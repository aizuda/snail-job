package com.aizuda.snail.job.server.job.task.support.block.job;

import com.aizuda.snail.job.common.core.enums.JobOperationReasonEnum;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import com.aizuda.snail.job.server.job.task.support.JobTaskStopHandler;
import com.aizuda.snail.job.server.job.task.enums.BlockStrategyEnum;
import com.aizuda.snail.job.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.snail.job.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.snail.job.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.snail.job.server.job.task.support.stop.TaskStopJobContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class OverlayBlockStrategy extends AbstracJobBlockStrategy {
    private final JobTaskBatchGenerator jobTaskBatchGenerator;
    @Override
    public void doBlock(final BlockStrategyContext context) {

        // 重新生成任务
        JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
        jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);

        // 停止任务
        JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(context.getTaskType());
        TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(context);

        Integer operationReason = context.getOperationReason();
        if (Objects.isNull(context.getOperationReason()) || context.getOperationReason() == JobOperationReasonEnum.NONE.getReason()) {
            operationReason = JobOperationReasonEnum.JOB_OVERLAY.getReason();
        }

        stopJobContext.setJobOperationReason(operationReason);
        stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
        instanceInterrupt.stop(stopJobContext);
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.OVERLAY;
    }
}
