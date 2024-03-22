package com.aizuda.easy.retry.server.job.task.support.callback;

import akka.actor.ActorRef;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.akka.ActorGenerator;
import com.aizuda.easy.retry.server.common.util.ClientInfoUtils;
import com.aizuda.easy.retry.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.easy.retry.server.job.task.enums.JobRetrySceneEnum;
import com.aizuda.easy.retry.server.job.task.support.ClientCallbackHandler;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    protected JobTaskMapper jobTaskMapper;
    @Autowired
    protected JobMapper jobMapper;

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
                realJobExecutor.setRetryCount(jobTask.getRetryCount() + 1);
                realJobExecutor.setRetry(Boolean.TRUE);
                realJobExecutor.setRetryScene(context.getRetryScene());
                ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
                actorRef.tell(realJobExecutor, actorRef);
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
        } else {
            context.setClientInfo(context.getJobTask().getClientInfo());
        }

        Job job = context.getJob();
        LambdaUpdateWrapper<JobTask> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(JobTask::getId, context.getTaskId());
        if (Objects.isNull(context.getRetryScene())
                || Objects.equals(JobRetrySceneEnum.AUTO.getRetryScene(), context.getRetryScene())) {
            updateWrapper.lt(JobTask::getRetryCount, job.getMaxRetryTimes());
        }

        return SqlHelper.retBool(jobTaskMapper.update(updateJobTask,updateWrapper));

    }

    private boolean isNeedRetry(ClientCallbackContext context) {

        JobTask jobTask = jobTaskMapper.selectById(context.getTaskId());
        Job job = jobMapper.selectById(context.getJobId());
        context.setJob(job);
        context.setJobTask(jobTask);
        if (Objects.isNull(jobTask) || Objects.isNull(job)) {
            return Boolean.FALSE;
        }

        // 手动重试策略
        if (Objects.nonNull(context.getRetryScene())
            && Objects.equals(JobRetrySceneEnum.MANUAL.getRetryScene(), context.getRetryScene())
            && !context.isRetry()) {
            return Boolean.TRUE;
        }

        if (context.getTaskStatus().equals(JobTaskStatusEnum.FAIL.getStatus())) {
            if (jobTask.getRetryCount() < job.getMaxRetryTimes()) {
                context.setRetryScene(JobRetrySceneEnum.AUTO.getRetryScene());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    protected abstract String chooseNewClient(ClientCallbackContext context);

    protected abstract void doCallback(ClientCallbackContext context);

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientCallbackFactory.registerJobExecutor(getTaskInstanceType(), this);
    }
}
