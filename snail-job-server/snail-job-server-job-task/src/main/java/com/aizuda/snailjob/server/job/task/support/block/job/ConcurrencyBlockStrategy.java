package com.aizuda.snailjob.server.job.task.support.block.job;

import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.enums.BlockStrategyEnum;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.batch.JobTaskBatchGeneratorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author: xiaowoniu
 * @date : 2024-01-18
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class ConcurrencyBlockStrategy extends AbstracJobBlockStrategy {
    private final JobTaskBatchGenerator jobTaskBatchGenerator;

    @Override
    public void doBlock(final BlockStrategyContext context) {
        // 重新生成任务
        JobTaskBatchGeneratorContext jobTaskBatchGeneratorContext = JobTaskConverter.INSTANCE.toJobTaskGeneratorContext(context);
        jobTaskBatchGenerator.generateJobTaskBatch(jobTaskBatchGeneratorContext);
    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.CONCURRENCY;
    }
}
