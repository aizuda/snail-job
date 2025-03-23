package com.aizuda.snailjob.server.job.task.support.timer;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.WorkflowNodeTaskExecuteDTO;
import com.aizuda.snailjob.server.job.task.dto.WorkflowTimerTaskDTO;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * @author: xiaowoniu
 * @date : 2023-09-25
 * @since 2.6.0
 */
@AllArgsConstructor
public class WorkflowTimerTask implements TimerTask<String> {
    public static final String IDEMPOTENT_KEY_PREFIX = "workflow_{0}";

    private WorkflowTimerTaskDTO workflowTimerTaskDTO;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        SnailJobLog.LOCAL.debug("开始执行任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), workflowTimerTaskDTO.getWorkflowTaskBatchId());

        try {

            WorkflowNodeTaskExecuteDTO taskExecuteDTO = new WorkflowNodeTaskExecuteDTO();
            taskExecuteDTO.setWorkflowTaskBatchId(workflowTimerTaskDTO.getWorkflowTaskBatchId());
            taskExecuteDTO.setTaskExecutorScene(workflowTimerTaskDTO.getTaskExecutorScene());
            taskExecuteDTO.setParentId(SystemConstants.ROOT);
            ActorRef actorRef = ActorGenerator.workflowTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, workflowTimerTaskDTO.getWorkflowTaskBatchId());
    }
}
