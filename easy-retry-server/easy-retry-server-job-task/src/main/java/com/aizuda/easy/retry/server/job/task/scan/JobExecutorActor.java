package com.aizuda.easy.retry.server.job.task.scan;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.easy.retry.client.model.DispatchJobDTO;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.client.RpcClient;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.job.task.dto.TaskExecuteDTO;
import com.aizuda.easy.retry.template.datasource.access.AccessTemplate;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-25 17:41
 */
@Component(ActorGenerator.JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class JobExecutorActor extends AbstractActor {

    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(TaskExecuteDTO.class, taskExecute -> {
            try {
                doExecute(taskExecute);
            } catch (Exception e) {
                LogUtils.error(log, "job executor exception. [{}]", taskExecute, e);
            }
        }).build();
    }

    private void doExecute(final TaskExecuteDTO taskExecute) {
        // 调度客户端
        JobTask jobTask = jobTaskMapper.selectById(taskExecute.getTaskId());
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(jobTask.getGroupName(), jobTask.getHostId());
        RpcClient rpcClient = RequestBuilder.<RpcClient, Result>newBuilder()
            .hostPort(registerNodeInfo.getHostPort())
            .groupName(registerNodeInfo.getGroupName())
            .hostId(registerNodeInfo.getHostId())
            .hostIp(registerNodeInfo.getHostIp())
            .contextPath(registerNodeInfo.getContextPath())
            .client(RpcClient.class)
            .build();

        DispatchJobDTO dispatchJobDTO = new DispatchJobDTO();
        dispatchJobDTO.setJobId(jobTask.getJobId());
        dispatchJobDTO.setTaskId(jobTask.getId());
        dispatchJobDTO.setGroupName(jobTask.getGroupName());
        Result dispatch = rpcClient.dispatch(dispatchJobDTO);

    }
}
