package com.aizuda.snailjob.server.job.task.support.stop;

import akka.actor.ActorRef;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.dto.RealStopTaskInstanceDTO;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author opensnail
 * @date 2023-10-02 12:59:53
 * @since 2.4.0
 */
@Component
@Slf4j
public class ShardingTaskStopHandler extends AbstractJobTaskStopHandler {

    @Override
    public JobTaskTypeEnum getTaskType() {
        return JobTaskTypeEnum.SHARDING;
    }

    @Override
    public void doStop(TaskStopJobContext context) {

        for (final JobTask jobTask : context.getJobTasks()) {
            RealStopTaskInstanceDTO taskInstanceDTO = JobTaskConverter.INSTANCE.toRealStopTaskInstanceDTO(context);
            taskInstanceDTO.setClientId(ClientInfoUtils.clientId(jobTask.getClientInfo()));

            ActorRef actorRef = ActorGenerator.jobRealStopTaskInstanceActor();
            actorRef.tell(taskInstanceDTO, actorRef);
        }

    }

}
