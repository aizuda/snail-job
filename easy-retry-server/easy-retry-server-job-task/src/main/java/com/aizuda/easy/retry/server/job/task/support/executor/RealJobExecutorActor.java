package com.aizuda.easy.retry.server.job.task.support.executor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.client.RequestBuilder;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.client.JobRpcClient;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.server.job.task.dto.JobLogDTO;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author www.byteblogs.com
 * @date 2023-10-06 16:42:08
 * @since 2.4.0
 */
@Component(ActorGenerator.REAL_JOB_EXECUTOR_ACTOR)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
public class RealJobExecutorActor extends AbstractActor {

    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RealJobExecutorDTO.class, realJobExecutorDTO -> {
            try {
                doExecute(realJobExecutorDTO);
            } catch (Exception e) {
                log.error("请求客户端发生异常", e);
            }
        }).build();
    }

    private void doExecute(RealJobExecutorDTO realJobExecutorDTO) {
        // 检查客户端是否存在
        RegisterNodeInfo registerNodeInfo = CacheRegisterTable.getServerNode(realJobExecutorDTO.getGroupName(),
            realJobExecutorDTO.getClientId());
        if (Objects.isNull(registerNodeInfo)) {
            taskExecuteFailure(realJobExecutorDTO, "无可执行的客户端");
            return;
        }

        JobLogDTO jobLogDTO = JobTaskConverter.INSTANCE.toJobLogDTO(realJobExecutorDTO);
        DispatchJobRequest dispatchJobRequest = JobTaskConverter.INSTANCE.toDispatchJobRequest(realJobExecutorDTO);

        try {
            // 构建请求客户端对象
            JobRpcClient rpcClient = buildRpcClient(registerNodeInfo, realJobExecutorDTO);
            Result<Boolean> dispatch = rpcClient.dispatch(dispatchJobRequest);
            if (dispatch.getStatus() == StatusEnum.YES.getStatus() && Objects.equals(dispatch.getData(),
                Boolean.TRUE)) {
                jobLogDTO.setMessage("任务调度成功");
            } else {
                jobLogDTO.setMessage(dispatch.getMessage());
            }

            ActorRef actorRef = ActorGenerator.jobLogActor();
            actorRef.tell(jobLogDTO, actorRef);
        } catch (Exception e) {
            log.error("调用客户端失败.", e);
            Throwable throwable = e;
            if (e.getClass().isAssignableFrom(RetryException.class)) {
                RetryException re = (RetryException) e;
                throwable = re.getLastFailedAttempt().getExceptionCause();
                taskExecuteFailure(realJobExecutorDTO, throwable.getMessage());
            }
        }

    }

    public static class JobExecutorRetryListener implements RetryListener {

        private RealJobExecutorDTO realJobExecutorDTO;
        private JobTaskMapper jobTaskMapper;

        public JobExecutorRetryListener(final RealJobExecutorDTO realJobExecutorDTO,
            final JobTaskMapper jobTaskMapper) {
            this.realJobExecutorDTO = realJobExecutorDTO;
            this.jobTaskMapper = jobTaskMapper;
        }

        @Override
        public <V> void onRetry(final Attempt<V> attempt) {
            if (attempt.hasException()) {
                LogUtils.error(log, "任务调度失败. taskInstanceId:[{}] count:[{}]",
                    realJobExecutorDTO.getTaskBatchId(), attempt.getAttemptNumber(), attempt.getExceptionCause());
                JobTask jobTask = new JobTask();
                jobTask.setRetryCount((int) attempt.getAttemptNumber());
                jobTaskMapper.updateById(jobTask);
            }
        }
    }

    private JobRpcClient buildRpcClient(RegisterNodeInfo registerNodeInfo, RealJobExecutorDTO realJobExecutorDTO) {
        return RequestBuilder.<JobRpcClient, Result>newBuilder()
            .nodeInfo(registerNodeInfo)
            .failRetry(Boolean.TRUE)
            .retryTimes(realJobExecutorDTO.getMaxRetryTimes())
            .retryInterval(realJobExecutorDTO.getRetryInterval())
            .retryListener(new JobExecutorRetryListener(realJobExecutorDTO, jobTaskMapper))
            .client(JobRpcClient.class)
            .build();
    }

    private static void taskExecuteFailure(RealJobExecutorDTO realJobExecutorDTO, String message) {
        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        JobExecutorResultDTO jobExecutorResultDTO = new JobExecutorResultDTO();
        jobExecutorResultDTO.setTaskId(realJobExecutorDTO.getTaskId());
        jobExecutorResultDTO.setJobId(realJobExecutorDTO.getJobId());
        jobExecutorResultDTO.setTaskBatchId(realJobExecutorDTO.getTaskBatchId());
        jobExecutorResultDTO.setTaskStatus(JobTaskStatusEnum.FAIL.getStatus());
        jobExecutorResultDTO.setMessage(message);
        actorRef.tell(jobExecutorResultDTO, actorRef);
    }
}
