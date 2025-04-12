package com.aizuda.snailjob.server.job.task.support.timer;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;
import java.time.LocalDateTime;

@AllArgsConstructor
public class RetryJobTimerTask implements TimerTask<String> {
    public static final String IDEMPOTENT_KEY_PREFIX = "retry_job_{0}";
    private RealJobExecutorDTO jobExecutorDTO;

    @Override
    public void run(final Timeout timeout) throws Exception {
        // 执行任务调度
        SnailJobLog.LOCAL.debug("Start retry task scheduling. Current time:[{}] Task ID:[{}]", LocalDateTime.now(), jobExecutorDTO.getTaskBatchId());
        JobTimerWheel.clearCache(idempotentKey());
        try {
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(jobExecutorDTO, actorRef);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("Retry task scheduling execution failed", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, jobExecutorDTO.getTaskId());
    }
}
