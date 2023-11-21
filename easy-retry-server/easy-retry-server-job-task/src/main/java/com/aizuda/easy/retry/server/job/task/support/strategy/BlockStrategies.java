package com.aizuda.easy.retry.server.job.task.support.strategy;

import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:52
 */
@Slf4j
public class BlockStrategies {

    @AllArgsConstructor
    @Getter
    public enum BlockStrategyEnum {
        DISCARD(1, new DiscardBlockStrategy()),
        OVERLAY(2, new OverlayBlockStrategy()),
        CONCURRENCY(3, new ConcurrencyBlockStrategy());

        private final int blockStrategy;
        private final BlockStrategy strategy;

        public static BlockStrategy getBlockStrategy(int blockStrategy) {
            for (final BlockStrategyEnum value : BlockStrategyEnum.values()) {
                if (value.blockStrategy == blockStrategy) {
                    return value.getStrategy();
                }
            }

            throw new EasyRetryServerException("不符合的阻塞策略. blockStrategy:[{}]", blockStrategy);
        }

    }

    @Data
    public static class BlockStrategyContext {

        private Long jobId;

        private Long taskBatchId;

        private String groupName;

        /**
         * 任务类型
         */
        private Integer taskType;

        /**
         * 下次触发时间
         */
        private Long nextTriggerAt;

        private Integer operationReason;

    }

    private static final class DiscardBlockStrategy implements BlockStrategy {

        @Override
        public void block(final BlockStrategyContext context) {
            log.warn("阻塞策略为丢弃此次执行. taskBatchId:[{}]", context.getTaskBatchId());

            // 重新生成任务
            JobTaskBatchGenerator jobTaskBatchGenerator = SpringContext.getBeanByType(JobTaskBatchGenerator.class);
            JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
            jobTaskBatchGeneratorContext.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            jobTaskBatchGeneratorContext.setOperationReason(JobOperationReasonEnum.JOB_DISCARD.getReason());
            jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);
        }
    }

    private static final class OverlayBlockStrategy implements BlockStrategy {

        @Override
        public void block(final BlockStrategyContext context) {
            log.warn("阻塞策略为覆盖. taskBatchId:[{}]", context.getTaskBatchId());

            // 重新生成任务
            JobTaskBatchGenerator jobTaskBatchGenerator = SpringContext.getBeanByType(JobTaskBatchGenerator.class);
            JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
            jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);

            // 停止任务
            JobTaskStopHandler instanceInterrupt = JobTaskStopFactory.getJobTaskStop(context.taskType);
            TaskStopJobContext stopJobContext = JobTaskConverter.INSTANCE.toStopJobContext(context);

            Integer operationReason = context.getOperationReason();
            if (Objects.isNull(context.getOperationReason()) || context.getOperationReason() == JobOperationReasonEnum.NONE.getReason()) {
                operationReason = JobOperationReasonEnum.JOB_OVERLAY.getReason();
            }

            stopJobContext.setJobOperationReason(operationReason);
            stopJobContext.setNeedUpdateTaskStatus(Boolean.TRUE);
            instanceInterrupt.stop(stopJobContext);

        }
    }

    private static final class ConcurrencyBlockStrategy implements BlockStrategy {

        @Override
        public void block(final BlockStrategyContext context) {
            log.warn("阻塞策略为并行执行. taskBatchId:[{}]", context.getTaskBatchId());

            // 重新生成任务
            JobTaskBatchGenerator jobTaskBatchGenerator = SpringContext.getBeanByType(JobTaskBatchGenerator.class);
            JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
            jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);
        }
    }


}
