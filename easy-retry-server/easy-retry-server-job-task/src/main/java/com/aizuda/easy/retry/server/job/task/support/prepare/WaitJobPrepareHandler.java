package com.aizuda.easy.retry.server.job.task.support.prepare;

import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * 处理处于{@link JobTaskBatchStatusEnum::WAIT}状态的任务
 *
 * @author www.byteblogs.com
 * @date 2023-10-05 18:29:22
 * @since 2.4.0
 */
@Component
@Slf4j
public class WaitJobPrepareHandler extends AbstractJobPrePareHandler {

    @Override
    public boolean matches(Integer status) {
        return JobTaskBatchStatusEnum.WAITING.getStatus() == status;
    }

    @Override
    protected void doHandler(JobTaskPrepareDTO jobPrepareDTO) {
        log.info("存在待处理任务. taskBatchId:[{}]", jobPrepareDTO.getTaskBatchId());

        // 若时间轮中数据不存在则重新加入
        if (!JobTimerWheel.isExisted(jobPrepareDTO.getTaskBatchId())) {

            // 进入时间轮
            long delay = jobPrepareDTO.getNextTriggerAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - System.currentTimeMillis();
            JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
            jobTimerTaskDTO.setTaskBatchId(jobPrepareDTO.getTaskBatchId());
            jobTimerTaskDTO.setJobId(jobPrepareDTO.getJobId());
            jobTimerTaskDTO.setGroupName(jobPrepareDTO.getGroupName());

            JobTimerWheel.register(jobPrepareDTO.getTaskBatchId(),
                    new JobTimerTask(jobTimerTaskDTO), delay, TimeUnit.MILLISECONDS);
        }
    }

}
