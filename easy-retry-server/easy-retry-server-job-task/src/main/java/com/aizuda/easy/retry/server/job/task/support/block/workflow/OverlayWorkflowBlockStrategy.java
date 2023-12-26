package com.aizuda.easy.retry.server.job.task.support.block.workflow;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowBatchGenerator;
import com.aizuda.easy.retry.server.job.task.support.generator.batch.WorkflowTaskBatchGeneratorContext;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.easy.retry.server.job.task.support.stop.JobTaskStopFactory;
import com.aizuda.easy.retry.server.job.task.support.stop.TaskStopJobContext;
import com.aizuda.easy.retry.server.job.task.support.strategy.BlockStrategies;
import com.aizuda.easy.retry.server.job.task.support.strategy.BlockStrategies.BlockStrategyEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum.NOT_COMPLETE;

/**
 * @author: xiaowoniu
 * @date : 2023-12-26
 * @since : 2.6.0
 */
@Component
@RequiredArgsConstructor
public class OverlayWorkflowBlockStrategy extends AbstractWorkflowBlockStrategy {

    private final WorkflowBatchHandler workflowBatchHandler;
    private final WorkflowBatchGenerator workflowBatchGenerator;

    @Override
    protected void doBlock(final WorkflowBlockStrategyContext workflowBlockStrategyContext) {

        // 停止当前批次
        workflowBatchHandler.stop(workflowBlockStrategyContext.getWorkflowTaskBatchId(), workflowBlockStrategyContext.getOperationReason());

        // 重新生成一个批次
        WorkflowTaskBatchGeneratorContext workflowTaskBatchGeneratorContext = WorkflowTaskConverter.INSTANCE.toWorkflowTaskBatchGeneratorContext(
            workflowBlockStrategyContext);
        workflowBatchGenerator.generateJobTaskBatch(workflowTaskBatchGeneratorContext);

    }

    @Override
    protected BlockStrategyEnum blockStrategyEnum() {
        return BlockStrategyEnum.OVERLAY;
    }
}
