package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import lombok.RequiredArgsConstructor;
import  org.apache.pekko.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-07 10:24
 * @since : 2.4.0
 */
@Component
@RequiredArgsConstructor
public class ShardingClientCallbackHandler extends AbstractClientCallbackHandler {
    private final InstanceManager instanceManager;
    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.SHARDING;
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

    @Override
    protected String chooseNewClient(ClientCallbackContext context) {
        Set<InstanceLiveInfo> instanceALiveInfoSet = instanceManager.getInstanceALiveInfoSet(
                context.getNamespaceId(), context.getGroupName(), context.getLabels());
        Set<RegisterNodeInfo> nodes = StreamUtils.toSet(instanceALiveInfoSet, InstanceLiveInfo::getNodeInfo);
        if (CollUtil.isEmpty(nodes)) {
            SnailJobLog.LOCAL.error("No executable client information. Job ID:[{}]", context.getJobId());
            return null;
        }

        RegisterNodeInfo serverNode = RandomUtil.randomEle(nodes.toArray(new RegisterNodeInfo[0]));
        return ClientInfoUtils.generate(serverNode);
    }
}
