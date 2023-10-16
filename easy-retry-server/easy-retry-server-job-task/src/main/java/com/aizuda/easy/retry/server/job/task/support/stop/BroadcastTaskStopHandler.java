package com.aizuda.easy.retry.server.job.task.support.stop;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.RealStopTaskInstanceDTO;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 12:59:53
 * @since 2.4.0
 */
@Component
@Slf4j
public class BroadcastTaskStopHandler extends AbstractJobTaskStopHandler {

    @Override
    public TaskTypeEnum getTaskType() {
        return TaskTypeEnum.BROADCAST;
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
