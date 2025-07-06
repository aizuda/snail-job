package com.aizuda.snailjob.server.job.task.support.prepare.workflow;

import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.WorkflowTimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Objects;

/**
 * 处理处于{@link JobTaskBatchStatusEnum::WAIT}状态的任务
 *
 * @author xiaowoniu
 * @date 2023-10-05 18:29:22
 * @since 2.6.0
 */
@Component
@Slf4j
public class WaitWorkflowPrepareHandler extends AbstractWorkflowPrePareHandler {

    @Override
    public boolean matches(Integer status) {
        return Objects.nonNull(status) && JobTaskBatchStatusEnum.WAITING.getStatus() == status;
    }

    @Override
    protected void doHandler(WorkflowTaskPrepareDTO workflowTaskPrepareDTO) {
        log.debug("Pending tasks exist. Workflow task batch ID:[{}]", workflowTaskPrepareDTO.getWorkflowTaskBatchId());

        // 若时间轮中数据不存在则重新加入
        if (!JobTimerWheel.isExisted(MessageFormat.format(WorkflowTimerTask.IDEMPOTENT_KEY_PREFIX, workflowTaskPrepareDTO.getWorkflowTaskBatchId()))) {
            log.info("Pending tasks exist and workflowTaskBatchId:[{}] does not exist in the time wheel", workflowTaskPrepareDTO.getWorkflowTaskBatchId());

            // 进入时间轮
            long delay = workflowTaskPrepareDTO.getNextTriggerAt() - DateUtils.toNowMilli();
            WorkflowTimerTaskDTO workflowTimerTaskDTO = new WorkflowTimerTaskDTO();
            workflowTimerTaskDTO.setWorkflowTaskBatchId(workflowTaskPrepareDTO.getWorkflowTaskBatchId());
            workflowTimerTaskDTO.setWorkflowId(workflowTaskPrepareDTO.getWorkflowId());
            workflowTimerTaskDTO.setTaskExecutorScene(workflowTaskPrepareDTO.getTaskExecutorScene());

            JobTimerWheel.registerWithWorkflow(() -> new WorkflowTimerTask(workflowTimerTaskDTO), Duration.ofMillis(delay));
        }
    }
}
