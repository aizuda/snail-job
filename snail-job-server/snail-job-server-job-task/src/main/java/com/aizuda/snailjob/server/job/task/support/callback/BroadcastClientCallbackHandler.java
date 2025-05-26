package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.server.common.dto.InstanceKey;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import lombok.RequiredArgsConstructor;
import  org.apache.pekko.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-07 10:24
 * @since : 2.4.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BroadcastClientCallbackHandler extends AbstractClientCallbackHandler {
    private final InstanceManager instanceManager;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.BROADCAST;
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
        Set<RegisterNodeInfo> nodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollUtil.isEmpty(nodes)) {
            log.error("No executable client information. Job ID:[{}]", context.getJobId());
            return null;
        }

        JobTask jobTask = context.getJobTask();
        String clientInfo = jobTask.getClientInfo();
        String clientId = ClientInfoUtils.clientId(clientInfo);
        InstanceKey instanceKey = InstanceKey.builder()
                .groupName(context.getGroupName())
                .namespaceId(context.getNamespaceId())
                .hostId(clientId)
                .build();
        InstanceLiveInfo instanceLiveInfo = instanceManager.getInstanceALiveInfoSet(instanceKey);
        if (Objects.isNull(instanceLiveInfo)) {
            List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                    .eq(JobTask::getTaskBatchId, context.getTaskBatchId()));

            Set<String> remoteClientIdSet = StreamUtils.toSet(nodes, RegisterNodeInfo::getHostId);
            Sets.SetView<String> diff = Sets.difference(remoteClientIdSet, getClientIdList(jobTasks));

            String newClientId = CollUtil.getFirst(diff.stream().iterator());
            instanceKey = InstanceKey.builder()
                    .groupName(context.getGroupName())
                    .namespaceId(context.getNamespaceId())
                    .hostId(newClientId)
                    .build();
            InstanceLiveInfo newInstanceLiveInfo = instanceManager.getInstanceALiveInfoSet(instanceKey);
            if (Objects.isNull(newInstanceLiveInfo)) {
                // 如果找不到新的客户端信息，则返回原来的客户端信息
                return clientInfo;
            }

            return ClientInfoUtils.generate(newInstanceLiveInfo.getNodeInfo());
        }

        return clientInfo;
    }

    private static Set<String> getClientIdList(List<JobTask> jobTasks) {
        return StreamUtils.toSet(jobTasks, jobTask -> ClientInfoUtils.clientId(jobTask.getClientInfo()));
    }
}
