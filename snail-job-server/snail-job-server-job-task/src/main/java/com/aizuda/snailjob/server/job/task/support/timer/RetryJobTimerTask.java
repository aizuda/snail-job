package com.aizuda.snailjob.server.job.task.support.timer;

import akka.actor.ActorRef;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.time.LocalDateTime;

@AllArgsConstructor
@Slf4j
public class RetryJobTimerTask implements TimerTask<String> {
    public static final String IDEMPOTENT_KEY_PREFIX = "retry_job_{0}";
    private RealJobExecutorDTO jobExecutorDTO;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        log.debug("开始执行重试任务调度. 当前时间:[{}] taskId:[{}]", LocalDateTime.now(), jobExecutorDTO.getTaskBatchId());

        try {
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(jobExecutorDTO, actorRef);
        } catch (Exception e) {
            log.error("重试任务调度执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, jobExecutorDTO.getTaskBatchId());
    }
}
