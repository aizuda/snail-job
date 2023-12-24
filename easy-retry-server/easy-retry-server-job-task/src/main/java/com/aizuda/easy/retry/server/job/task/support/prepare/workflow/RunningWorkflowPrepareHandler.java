package com.aizuda.easy.retry.server.job.task.support.prepare.workflow;

import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.support.handler.WorkflowBatchHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
        return JobTaskBatchStatusEnum.RUNNING.getStatus() == status;
    }

    @Override
    protected void doHandler(WorkflowTaskPrepareDTO jobPrepareDTO) {
        log.info("存在运行中的任务. prepare:[{}]", JsonUtil.toJsonString(jobPrepareDTO));

        // 1. 若DAG已经支持完成了，由于异常原因导致的没有更新成终态此次进行一次更新操作
        try {
            workflowBatchHandler.complete(jobPrepareDTO.getWorkflowTaskBatchId());
        } catch (IOException e) {
            // TODO 待处理
        }

        // 2. 判断DAG是否已经支持超时
        // 3. 支持阻塞策略同JOB逻辑一致


    }
}
