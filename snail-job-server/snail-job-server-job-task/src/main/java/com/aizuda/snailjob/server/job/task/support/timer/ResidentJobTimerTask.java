package com.aizuda.snailjob.server.job.task.support.timer;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.TimerTask;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import io.netty.util.Timeout;
import lombok.AllArgsConstructor;

import java.text.MessageFormat;

/**
 * @author opensnail
 * @date 2023-10-20 23:09:13
 * @since 2.4.0
 */
@AllArgsConstructor
public class ResidentJobTimerTask implements TimerTask<String> {
    private static final String IDEMPOTENT_KEY_PREFIX = " resident_job_{0}";

    private JobTimerTaskDTO jobTimerTaskDTO;
    private Job job;

    @Override
    public void run(Timeout timeout) throws Exception {
        try {
            // 清除时间轮的缓存
            JobTimerWheel.clearCache(idempotentKey());
            JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
            jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_JOB.getType());
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
            actorRef.tell(jobTaskPrepare, actorRef);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("任务调度执行失败", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, jobTimerTaskDTO.getTaskBatchId());
    }
}
