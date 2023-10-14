package com.aizuda.easy.retry.server.job.task.support.dispatch;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.server.job.task.support.JobExecutor;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.support.executor.JobExecutorContext;
import com.aizuda.easy.retry.server.job.task.support.executor.JobExecutorFactory;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:41
 * @since : 2.4.0
 */
@Component(ActorGenerator.JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobExecutorActor extends AbstractActor {
    @Autowired
    private JobMapper jobMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskExecuteDTO.class, taskExecute -> {
            try {
                doExecute(taskExecute);
            } catch (Exception e) {
                LogUtils.error(log, "job executor exception. [{}]", taskExecute, e);
            } finally {
                getContext().stop(getSelf());
            }
        }).build();
    }

    private void doExecute(final TaskExecuteDTO taskExecute) {
        Job job = jobMapper.selectById(taskExecute.getJobId());
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(job.getTaskType());

        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskBatchId(taskExecute.getTaskBatchId());
        context.setJobId(job.getId());
        context.setTaskType(job.getTaskType());
        jobExecutor.execute(context);

    }
}
