package com.aizuda.easy.retry.server.job.task.support.timer;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:28
 * @since 2.4.0
 */
@AllArgsConstructor
@Slf4j
public class JobTimerTask implements TimerTask {

    private JobTimerTaskDTO jobTimerTaskDTO;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        log.info("开始执行任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), jobTimerTaskDTO.getTaskBatchId());

        try {
            TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
            taskExecuteDTO.setTaskBatchId(jobTimerTaskDTO.getTaskBatchId());
            taskExecuteDTO.setJobId(jobTimerTaskDTO.getJobId());
            ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
            actorRef.tell(taskExecuteDTO, actorRef);

        } catch (Exception e) {
            log.error("任务调度执行失败", e);
        }
    }

}
