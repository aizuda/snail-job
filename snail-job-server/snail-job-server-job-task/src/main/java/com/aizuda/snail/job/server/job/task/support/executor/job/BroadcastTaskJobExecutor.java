package com.aizuda.snail.job.server.job.task.support.executor.job;

import akka.actor.ActorRef;
import com.aizuda.snail.job.server.common.akka.ActorGenerator;
import com.aizuda.snail.job.server.common.util.ClientInfoUtils;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import com.aizuda.snail.job.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.snail.job.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author opensnail
 * @date 2023-10-06 10:27:26
 * @since 2.4.0
 */
@Component
@Slf4j
public class BroadcastTaskJobExecutor extends AbstractJobExecutor {

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.BROADCAST;
    }

    @Override
    protected void doExecute(JobExecutorContext context) {

        List<JobTask> taskList = context.getTaskList();

        for (JobTask jobTask : taskList) {
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, jobTask);
            realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(realJobExecutor, actorRef);
        }

    }

}
