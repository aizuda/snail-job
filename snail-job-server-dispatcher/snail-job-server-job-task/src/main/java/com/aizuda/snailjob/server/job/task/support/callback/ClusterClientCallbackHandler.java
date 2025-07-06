package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.InstanceSelectCondition;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import  org.apache.pekko.actor.ActorRef;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
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
    private final InstanceManager instanceManager;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.CLUSTER;
    }

    @Override
    protected String chooseNewClient(ClientCallbackContext context) {

        // 选择重试的节点
        InstanceSelectCondition condition = InstanceSelectCondition
                .builder()
                .allocKey(String.valueOf(context.getJobId()))
                .groupName(context.getGroupName())
                .namespaceId(context.getNamespaceId())
                .routeKey(context.getJob().getRouteKey())
                .targetLabels(context.getJob().getLabels())
                .build();
        InstanceLiveInfo instance = instanceManager.getALiveInstanceByRouteKey(condition);

        if (Objects.isNull(instance)) {
            log.error("No executable client information. Job ID:[{}]", context.getJobId());
            return null;
        }

        return ClientInfoUtils.generate(instance.getNodeInfo());

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
