package com.aizuda.easy.retry.server.job.task.handler.callback;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-07 10:24
 * @since : 2.4.0
 */
@Component
@Slf4j
public class BroadcastClientCallbackHandler extends AbstractClientCallbackHandler {

    @Override
    public TaskTypeEnum getTaskInstanceType() {
        return TaskTypeEnum.BROADCAST;
    }

    @Override
    protected void doCallback(final ClientCallbackContext context) {
        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType().getType());

        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        actorRef.tell(jobExecutorResultDTO, actorRef);

    }

}
