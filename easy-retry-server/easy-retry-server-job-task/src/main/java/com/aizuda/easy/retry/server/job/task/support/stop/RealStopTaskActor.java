package com.aizuda.easy.retry.server.job.task.support.stop;

import akka.actor.AbstractActor;
import com.aizuda.easy.retry.client.model.StopJobDTO;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.job.task.client.JobRpcClient;
import com.aizuda.easy.retry.server.job.task.dto.RealStopTaskInstanceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-07 10:45
 * @since : 2.4.0
 */
@Component(ActorGenerator.REAL_STOP_TASK_INSTANCE_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class RealStopTaskActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RealStopTaskInstanceDTO.class, realStopTaskInstanceDTO -> {
            try {
                doStop(realStopTaskInstanceDTO);
            } catch (Exception e) {
                log.error("停止任务执行失败", e);
            }
        }).build();
    }

    private void doStop(final RealStopTaskInstanceDTO realStopTaskInstanceDTO) {

        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(
                realStopTaskInstanceDTO.getGroupName(),
                realStopTaskInstanceDTO.getNamespaceId(),
                realStopTaskInstanceDTO.getClientId());
        if (Objects.nonNull(registerNodeInfo)) {
            // 不用关心停止的结果，若服务端尝试终止失败,客户端会兜底进行关闭
            requestClient(realStopTaskInstanceDTO, registerNodeInfo);
        }
    }

    private Result<Boolean> requestClient(RealStopTaskInstanceDTO realStopTaskInstanceDTO, RegisterNodeInfo registerNodeInfo) {
        JobRpcClient rpcClient = RequestBuilder.<JobRpcClient, Result>newBuilder()
                .nodeInfo(registerNodeInfo)
                .failRetry(Boolean.TRUE)
                .retryTimes(3)
                .retryInterval(1)
                .client(JobRpcClient.class)
                .build();

        StopJobDTO stopJobDTO = new StopJobDTO();
        stopJobDTO.setTaskBatchId(realStopTaskInstanceDTO.getTaskBatchId());
        stopJobDTO.setJobId(realStopTaskInstanceDTO.getJobId());
        stopJobDTO.setGroupName(realStopTaskInstanceDTO.getGroupName());
        return rpcClient.stop(stopJobDTO);
    }
}
