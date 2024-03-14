package com.aizuda.easy.retry.server.job.task.support.prepare.workflow;

import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.BlockStrategy;
import com.aizuda.easy.retry.server.job.task.support.WorkflowTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.block.workflow.WorkflowBlockStrategyContext;
import com.aizuda.easy.retry.server.job.task.support.block.workflow.WorkflowBlockStrategyFactory;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
import com.aizuda.easy.retry.server.job.task.enums.BlockStrategyEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2023-12-23 23:09:07
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RunningWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {

    private final WorkflowBatchHandler workflowBatchHandler;

    @Override
    public boolean matches(Integer status) {
        return Objects.nonNull(status) && JobTaskBatchStatusEnum.RUNNING.getStatus() == status;
    }

    @Override
    protected void doHandler(WorkflowTaskPrepareDTO prepare) {
        log.debug("存在运行中的任务. prepare:[{}]", JsonUtil.toJsonString(prepare));


        // 1. 若DAG已经支持完成了，由于异常原因导致的没有更新成终态此次进行一次更新操作
        int blockStrategy = prepare.getBlockStrategy();
        if (workflowBatchHandler.complete(prepare.getWorkflowTaskBatchId())) {
            // 开启新的任务
            blockStrategy = BlockStrategyEnum.CONCURRENCY.getBlockStrategy();
        } else {
            // 计算超时时间
            long delay = DateUtils.toNowMilli() - prepare.getExecutionAt();

            // 2. 判断DAG是否已经支持超时
            // 计算超时时间，到达超时时间中断任务
            if (delay > DateUtils.toEpochMilli(prepare.getExecutorTimeout())) {
                log.info("任务执行超时.workflowTaskBatchId:[{}] delay:[{}] executorTimeout:[{}]",
                        prepare.getWorkflowTaskBatchId(), delay, DateUtils.toEpochMilli(prepare.getExecutorTimeout()));
                // 超时停止任务
                workflowBatchHandler.stop(prepare.getWorkflowTaskBatchId(), JobOperationReasonEnum.TASK_EXECUTION_TIMEOUT.getReason());
            }
        }

        // 仅是超时检测的，不执行阻塞策略
        if (prepare.isOnlyTimeoutCheck()) {
            return;
        }

        // 3. 支持阻塞策略同JOB逻辑一致
        BlockStrategy blockStrategyInterface = WorkflowBlockStrategyFactory.getBlockStrategy(blockStrategy);
        WorkflowBlockStrategyContext workflowBlockStrategyContext = WorkflowTaskConverter.INSTANCE.toWorkflowBlockStrategyContext(
                prepare);
        blockStrategyInterface.block(workflowBlockStrategyContext);

    }
}
