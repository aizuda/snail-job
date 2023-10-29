package com.aizuda.easy.retry.server.job.task.support.stop;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskStopHandler;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 09:48:10
 * @since 2.4.0
 */
public abstract class AbstractJobTaskStopHandler implements JobTaskStopHandler, InitializingBean {

    @Autowired
    private JobTaskMapper jobTaskMapper;

    protected abstract void doStop(TaskStopJobContext context);

    @Override
    public void stop(TaskStopJobContext context) {

        LambdaQueryWrapper<JobTask> queryWrapper = new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId());

        if (!context.isForceStop()) {
            queryWrapper.in(JobTask::getTaskStatus, JobTaskStatusEnum.NOT_COMPLETE);
        }

        List<JobTask> jobTasks = jobTaskMapper.selectList(queryWrapper);

        if (CollectionUtils.isEmpty(jobTasks)) {
            return;
        }

        context.setJobTasks(jobTasks);

        doStop(context);

        if (context.isNeedUpdateTaskStatus()) {
            for (final JobTask jobTask : jobTasks) {
                JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(jobTask);
                jobExecutorResultDTO.setTaskStatus(JobTaskStatusEnum.STOP.getStatus());
                jobExecutorResultDTO.setMessage("任务停止成功");
                jobExecutorResultDTO.setJobOperationReason(context.getJobOperationReason());
                jobExecutorResultDTO.setTaskType(getTaskType().getType());
                ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
                actorRef.tell(jobExecutorResultDTO, actorRef);
            }

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JobTaskStopFactory.registerTaskStop(getTaskType(), this);
    }
}
