package com.aizuda.snailjob.server.job.task.support.executor.job;

import akka.actor.ActorRef;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2024-06-12
 * @since : sj_1.1.0
 */
@Component
public class MapReduceJobExecutor extends AbstractJobExecutor  {

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected void doExecute(final JobExecutorContext context) {
        List<JobTask> taskList = context.getTaskList();
        for (int i = 0; i < taskList.size(); i++) {
            JobTask jobTask = taskList.get(i);
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(context, jobTask);
            realJobExecutor.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));
            ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
            actorRef.tell(realJobExecutor, actorRef);
        }
    }

}
