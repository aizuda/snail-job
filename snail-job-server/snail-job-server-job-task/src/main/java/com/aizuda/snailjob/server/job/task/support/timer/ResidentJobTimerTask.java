package com.aizuda.snailjob.server.job.task.support.timer;

import akka.actor.ActorRef;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author opensnail
 * @date 2023-10-20 23:09:13
 * @since 2.4.0
 */
@Slf4j
@AllArgsConstructor
public class ResidentJobTimerTask implements TimerTask {

    private JobTimerTaskDTO jobTimerTaskDTO;
    private Job job;

    @Override
    public void run(Timeout timeout) throws Exception {
        try {
            // 清除时间轮的缓存
            JobTimerWheel.clearCache(SyetemTaskTypeEnum.JOB.getType(), jobTimerTaskDTO.getTaskBatchId());
            JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
            jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.AUTO_JOB.getType());
            // 执行预处理阶段
            ActorRef actorRef = ActorGenerator.jobTaskPrepareActor();
            actorRef.tell(jobTaskPrepare, actorRef);
        } catch (Exception e) {
            log.error("任务调度执行失败", e);
        }
    }
}
