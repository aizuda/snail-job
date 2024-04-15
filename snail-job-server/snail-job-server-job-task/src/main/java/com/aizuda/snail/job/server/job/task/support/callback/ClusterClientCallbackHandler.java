package com.aizuda.snail.job.server.job.task.support.callback;

import akka.actor.ActorRef;
import com.aizuda.snail.job.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snail.job.server.common.akka.ActorGenerator;
import com.aizuda.snail.job.server.common.dto.RegisterNodeInfo;
import com.aizuda.snail.job.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snail.job.server.common.util.ClientInfoUtils;
import com.aizuda.snail.job.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-03 23:12:12
 * @since 2.4.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ClusterClientCallbackHandler extends AbstractClientCallbackHandler {
    private final ClientNodeAllocateHandler clientNodeAllocateHandler;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.CLUSTER;
    }

    @Override
    protected String chooseNewClient(ClientCallbackContext context) {

        // 选择重试的节点
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(context.getJobId().toString(),
                context.getGroupName(), context.getNamespaceId(), context.getJob().getRouteKey());
        if (Objects.isNull(serverNode)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return null;
        }

        return ClientInfoUtils.generate(serverNode);
    }

    @Override
    protected void doCallback(ClientCallbackContext context) {

        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType().getType());

        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        actorRef.tell(jobExecutorResultDTO, actorRef);

    }

}
