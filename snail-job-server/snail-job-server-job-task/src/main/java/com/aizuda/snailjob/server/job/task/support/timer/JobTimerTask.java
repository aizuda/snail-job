package com.aizuda.snailjob.server.job.task.support.timer;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.dto.TaskExecuteDTO;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.time.LocalDateTime;

/**
 * @author: opensnail
 * @date : 2023-09-25 17:28
 * @since 2.4.0
 */
@AllArgsConstructor
public class JobTimerTask implements TimerTask<String> {
    public static final String IDEMPOTENT_KEY_PREFIX = "job_{0}";
    private JobTimerTaskDTO jobTimerTaskDTO;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        SnailJobLog.LOCAL.debug("开始执行任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), jobTimerTaskDTO.getTaskBatchId());

        try {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(jobTimerTaskDTO.getTaskBatchId());
            taskExecuteDTO.setJobId(jobTimerTaskDTO.getJobId());
            taskExecuteDTO.setTaskExecutorScene(jobTimerTaskDTO.getTaskExecutorScene());
            taskExecuteDTO.setWorkflowTaskBatchId(jobTimerTaskDTO.getWorkflowTaskBatchId());
            taskExecuteDTO.setWorkflowNodeId(jobTimerTaskDTO.getWorkflowNodeId());
            taskExecuteDTO.setTmpArgsStr(jobTimerTaskDTO.getTmpArgsStr());
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

        } catch (Exception e) {
            SnailJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, jobTimerTaskDTO.getTaskBatchId());
    }
}
