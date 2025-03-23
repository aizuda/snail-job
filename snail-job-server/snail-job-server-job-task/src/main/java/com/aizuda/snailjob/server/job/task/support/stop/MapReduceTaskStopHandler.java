package com.aizuda.snailjob.server.job.task.support.stop;

import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.RealStopTaskInstanceDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import org.springframework.stereotype.Component;

/**
 * @author: opensnail
 * @date : 2024-06-13
 * @since : sj_1.1.0
 */
@Component
public class MapReduceTaskStopHandler extends AbstractJobTaskStopHandler {

    @Override
    public JobTaskTypeEnum getTaskType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected void doStop(final TaskStopJobContext context) {
        for (final JobTask jobTask : context.getJobTasks()) {
            RealStopTaskInstanceDTO taskInstanceDTO = JobTaskConverter.INSTANCE.toRealStopTaskInstanceDTO(context);
            taskInstanceDTO.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));

            ActorRef actorRef = ActorGenerator.jobRealStopTaskInstanceActor();
            actorRef.tell(taskInstanceDTO, actorRef);
        }
    }


}
