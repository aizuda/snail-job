package com.aizuda.snailjob.server.job.task.support.callback;

import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import  org.apache.pekko.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.pekko.ActorGenerator;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author: opensnail
 * @date : 2024-06-13 21:22x
 * @since : sj_1.1.0
 */
@Component
@RequiredArgsConstructor
public class MapReduceClientCallbackHandler extends AbstractClientCallbackHandler {
    private final JobTaskMapper jobTaskMapper;
    private final InstanceManager instanceManager;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected void doCallback(final ClientCallbackContext context) {
        JobTask jobTask = jobTaskMapper.selectOne(new LambdaQueryWrapper<JobTask>()
                .eq(JobTask::getId, context.getTaskId()));
        Assert.notNull(jobTask, () -> new SnailJobServerException("job task is null"));

        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType().getType());
        jobExecutorResultDTO.setIsLeaf(jobTask.getLeaf());
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
