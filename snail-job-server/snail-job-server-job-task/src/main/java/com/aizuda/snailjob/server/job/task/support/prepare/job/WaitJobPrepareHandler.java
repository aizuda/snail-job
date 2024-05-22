package com.aizuda.snailjob.server.job.task.support.prepare.job;

import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 处理处于{@link JobTaskBatchStatusEnum::WAIT}状态的任务
 *
 * @author opensnail
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
        log.debug("存在待处理任务. taskBatchId:[{}]", jobPrepareDTO.getTaskBatchId());

        // 若时间轮中数据不存在则重新加入
        if (!JobTimerWheel.isExisted(SyetemTaskTypeEnum.JOB.getType(), jobPrepareDTO.getTaskBatchId())) {
            log.info("存在待处理任务且时间轮中不存在 taskBatchId:[{}]", jobPrepareDTO.getTaskBatchId());

            // 进入时间轮
            long delay = jobPrepareDTO.getNextTriggerAt() - DateUtils.toNowMilli();
            JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
            jobTimerTaskDTO.setTaskBatchId(jobPrepareDTO.getTaskBatchId());
            jobTimerTaskDTO.setJobId(jobPrepareDTO.getJobId());

            JobTimerWheel.registerWithJob(() -> new JobTimerTask(jobTimerTaskDTO), Duration.ofMillis(delay));
//            JobTimerWheel.register(SyetemTaskTypeEnum.JOB.getType(), jobPrepareDTO.getTaskBatchId(),
//                    new JobTimerTask(jobTimerTaskDTO), delay, TimeUnit.MILLISECONDS);
        }
    }

}
