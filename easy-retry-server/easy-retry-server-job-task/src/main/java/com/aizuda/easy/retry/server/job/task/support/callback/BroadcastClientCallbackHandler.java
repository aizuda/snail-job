package com.aizuda.easy.retry.server.job.task.support.callback;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.utils.LambdaUpdateExpandWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-07 10:24
 * @since : 2.4.0
 */
@Component
@Slf4j
public class BroadcastClientCallbackHandler extends AbstractClientCallbackHandler {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobMapper jobMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.BROADCAST;
    }

    @Override
    protected void doCallback(final ClientCallbackContext context) {
        if (context.getTaskStatus().equals(JobTaskStatusEnum.FAIL.getStatus())) {
            JobTask jobTask = jobTaskMapper.selectById(context.getTaskId());
            Job job = jobMapper.selectById(context.getJobId());
            if (jobTask == null || job == null) {
                return;
            }
            if (jobTask.getRetryCount() < job.getMaxRetryTimes()) {
                // 更新重试次数
                jobTaskMapper.update(null, new LambdaUpdateExpandWrapper<>(JobTask.class)
                        .incrField(JobTask::getRetryCount, 1)
                        .lt(JobTask::getRetryCount, job.getMaxRetryTimes())
                        .eq(JobTask::getId, context.getTaskId())
                );
                RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(JobTaskConverter.INSTANCE.toJobExecutorContext(job), jobTask);
                realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
                ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
                actorRef.tell(realJobExecutor, actorRef);
                // TODO 记录日志
                return;
            }
        }
        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType().getType());

        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        actorRef.tell(jobExecutorResultDTO, actorRef);

    }

}
