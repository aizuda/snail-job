package com.aizuda.snailjob.server.job.task.support.prepare.job;

import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerTask;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
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
public class WaitJobPrepareHandler extends AbstractJobPrepareHandler {

    @Override
    public boolean matches(Integer status) {
        return JobTaskBatchStatusEnum.WAITING.getStatus() == status;
    }

    @Override
    protected void doHandle(JobTaskPrepareDTO jobPrepareDTO) {
        log.debug("Pending tasks exist. Task batch ID:[{}]", jobPrepareDTO.getTaskBatchId());

        // 若时间轮中数据不存在则重新加入
        if (!JobTimerWheel.isExisted(MessageFormat.format(JobTimerTask.IDEMPOTENT_KEY_PREFIX, jobPrepareDTO.getTaskBatchId()))) {
            log.info("Pending tasks exist and taskBatchId:[{}] does not exist in the time wheel", jobPrepareDTO.getTaskBatchId());

            // 进入时间轮
            long delay = jobPrepareDTO.getNextTriggerAt() - DateUtils.toNowMilli();
            JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
            jobTimerTaskDTO.setTaskBatchId(jobPrepareDTO.getTaskBatchId());
            jobTimerTaskDTO.setJobId(jobPrepareDTO.getJobId());

            JobTimerWheel.registerWithJob(() -> new JobTimerTask(jobTimerTaskDTO), Duration.ofMillis(delay));
        }
    }

}
