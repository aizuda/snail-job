package com.aizuda.easy.retry.server.job.task.support.callback;

import akka.actor.ActorRef;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.dto.JobExecutorResultDTO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-07 10:24
 * @since : 2.4.0
 */
@Component
@Slf4j
public class ShardingClientCallbackHandler extends AbstractClientCallbackHandler {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobMapper jobMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.SHARDING;
    }

    @Override
    protected void doCallback(final ClientCallbackContext context) {
        if (context.getTaskStatus().equals(JobTaskStatusEnum.FAIL.getStatus())) {
            JobTask jobTask = jobTaskMapper.selectById(context.getTaskId());
            Job job = jobMapper.selectById(context.getJobId());
            if (jobTask == null || job == null) {
                return;
            }
            if (jobTask.getRetryCount() < job.getMaxRetryTimes()) {
                Set<RegisterNodeInfo> nodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
                if (CollUtil.isEmpty(nodes)) {
                    log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
                    return;
                }
                RegisterNodeInfo serverNode = RandomUtil.randomEle(nodes.toArray(new RegisterNodeInfo[0]));
                String newClient = ClientInfoUtils.generate(serverNode);
                // 更新重试次数
                JobTask updateJobTask = new JobTask();
                updateJobTask.setClientInfo(newClient);
                updateJobTask.setRetryCount(1);
                boolean success = SqlHelper.retBool(
                        jobTaskMapper.update(updateJobTask, Wrappers.<JobTask>lambdaUpdate()
                                .lt(JobTask::getRetryCount, job.getMaxRetryTimes())
                                .eq(JobTask::getId, context.getTaskId())
                        )
                );
                if (success) {
                    RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(JobTaskConverter.INSTANCE.toJobExecutorContext(job), jobTask);
                    realJobExecutor.setClientId(ClientInfoUtils.clientId(newClient));
                    ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
                    actorRef.tell(realJobExecutor, actorRef);
                    // TODO 记录日志
                }
                return;
            }
        }

        JobExecutorResultDTO jobExecutorResultDTO = JobTaskConverter.INSTANCE.toJobExecutorResultDTO(context);
        jobExecutorResultDTO.setTaskId(context.getTaskId());
        jobExecutorResultDTO.setMessage(context.getExecuteResult().getMessage());
        jobExecutorResultDTO.setResult(context.getExecuteResult().getResult());
        jobExecutorResultDTO.setTaskType(getTaskInstanceType().getType());

        ActorRef actorRef = ActorGenerator.jobTaskExecutorResultActor();
        actorRef.tell(jobExecutorResultDTO, actorRef);
    }

}
