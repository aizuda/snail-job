package com.aizuda.easy.retry.server.job.task.support.callback;

import akka.actor.ActorRef;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.dto.LogMetaDTO;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author www.byteblogs.com
 * @date 2023-10-03 23:12:33
 * @since 2.4.0
 */
public abstract class AbstractClientCallbackHandler implements ClientCallbackHandler, InitializingBean {

    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobMapper jobMapper;

    @Override
    @Transactional
    public void callback(ClientCallbackContext context) {

        // 判定是否需要重试
        boolean needRetry = isNeedRetry(context);
        if (needRetry) {
            // 更新重试次数
            if (updateRetryCount(context)) {
                Job job = context.getJob();
                JobTask jobTask = context.getJobTask();
                RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(JobTaskConverter.INSTANCE.toJobExecutorContext(job), jobTask);
                realJobExecutor.setClientId(ClientInfoUtils.clientId(context.getClientInfo()));
                realJobExecutor.setWorkflowNodeId(context.getWorkflowNodeId());
                realJobExecutor.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
                ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
                actorRef.tell(realJobExecutor, actorRef);
                LogMetaDTO logMetaDTO = JobTaskConverter.INSTANCE.toJobLogDTO(context);
                EasyRetryLog.REMOTE.info("任务执行/调度失败执行重试. 重试次数:[{}] <|>{}<|>",
                        jobTask.getRetryCount() + 1, logMetaDTO);
                return;
            }
        }

        // 不需要重试执行回调
        doCallback(context);
    }

    private boolean updateRetryCount(ClientCallbackContext context) {
        JobTask updateJobTask = new JobTask();
        updateJobTask.setRetryCount(1);
        String newClient = chooseNewClient(context);
        if (StrUtil.isNotBlank(newClient)) {
            updateJobTask.setClientInfo(newClient);
            // 覆盖老的的客户端信息
            context.setClientInfo(newClient);
        }

        Job job = context.getJob();
        return SqlHelper.retBool(jobTaskMapper.update(updateJobTask, Wrappers.<JobTask>lambdaUpdate()
                .lt(JobTask::getRetryCount, job.getMaxRetryTimes())
                .eq(JobTask::getId, context.getTaskId())
        ));

    }

    private boolean isNeedRetry(ClientCallbackContext context) {

        if (context.getTaskStatus().equals(JobTaskStatusEnum.FAIL.getStatus())) {

            JobTask jobTask = jobTaskMapper.selectById(context.getTaskId());
            Job job = jobMapper.selectById(context.getJobId());
            if (Objects.isNull(jobTask) || Objects.isNull(job)) {
                return Boolean.FALSE;
            }

            if (jobTask.getRetryCount() < job.getMaxRetryTimes()) {
                context.setClientInfo(jobTask.getClientInfo());
                context.setJob(job);
                context.setJobTask(jobTask);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    protected String chooseNewClient(ClientCallbackContext context) {
        return null;
    }

    private void failRetry(ClientCallbackContext context) {

        if (context.getTaskStatus().equals(JobTaskStatusEnum.FAIL.getStatus())) {
            JobTask jobTask = jobTaskMapper.selectById(context.getTaskId());
            Job job = jobMapper.selectById(context.getJobId());
            if (jobTask == null || job == null) {
                return;
            }

            if (jobTask.getRetryCount() < job.getMaxRetryTimes()) {
                // 更新重试次数
                JobTask updateJobTask = new JobTask();
                updateJobTask.setRetryCount(1);
                boolean success = SqlHelper.retBool(jobTaskMapper.update(updateJobTask, Wrappers.<JobTask>lambdaUpdate()
                        .lt(JobTask::getRetryCount, job.getMaxRetryTimes())
                        .eq(JobTask::getId, context.getTaskId())
                ));

                if (success) {

                }

                return;
            }
        }
    }

    protected abstract void doCallback(ClientCallbackContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientCallbackFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
