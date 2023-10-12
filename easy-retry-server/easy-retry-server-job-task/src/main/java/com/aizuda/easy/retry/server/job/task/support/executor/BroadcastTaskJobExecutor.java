package com.aizuda.easy.retry.server.job.task.support.executor;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-06 10:27:26
 * @since 2.4.0
 */
@Component
@Slf4j
public class BroadcastTaskJobExecutor extends AbstractJobExecutor {

    @Override
    public TaskTypeEnum getTaskInstanceType() {
        return TaskTypeEnum.BROADCAST;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {

        Job job = context.getJob();
        List<JobTask> taskList = context.getTaskList();

        for (JobTask jobTask : taskList) {
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(job, jobTask);
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(realJobExecutor, actorRef);
        }

    }

}
