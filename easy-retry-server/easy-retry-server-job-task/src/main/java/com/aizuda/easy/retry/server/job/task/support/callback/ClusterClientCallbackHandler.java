package com.aizuda.easy.retry.server.job.task.support.callback;

import akka.actor.ActorRef;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 23:12:12
 * @since 2.4.0
 */
@Component
@Slf4j
public class ClusterClientCallbackHandler extends AbstractClientCallbackHandler {

    @Autowired
    private ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobMapper jobMapper;

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
