package com.aizuda.easy.retry.server.job.task.scan;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:28
 */
@AllArgsConstructor
@Slf4j
public class JobTimerTask implements TimerTask {

    private Long taskId;
    private String groupName;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        log.info("开始执行任务调度. 当前时间:[{}]", LocalDateTime.now());

        // 先清除时间轮的缓存
        JobTimerWheelHandler.clearCache(groupName, taskId.toString());

        TaskExecuteDTO taskExecuteDTO = new TaskExecuteDTO();
        ActorRef actorRef = ActorGenerator.jobTaskExecutorActor();
        actorRef.tell(taskExecuteDTO, actorRef);
    }
}
