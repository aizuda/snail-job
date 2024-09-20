package com.aizuda.snailjob.server.job.task.support.callback;

import akka.actor.ActorRef;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.akka.ActorGenerator;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.RealJobExecutorDTO;
import com.aizuda.snailjob.server.job.task.enums.JobRetrySceneEnum;
import com.aizuda.snailjob.server.job.task.support.ClientCallbackHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.snailjob.server.job.task.support.timer.RetryJobTimerTask;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-03 23:12:33
 * @since 2.4.0
 */
public abstract class AbstractClientCallbackHandler implements ClientCallbackHandler, InitializingBean {

    @Autowired
    protected JobTaskMapper jobTaskMapper;
    @Autowired
    private JobMapper jobMapper;
    @Autowired
    private WorkflowTaskBatchMapper workflowTaskBatchMapper;


    @Override
    @Transactional
    public void callback(ClientCallbackContext context) {

        // 判定是否需要重试
        boolean needRetry = isNeedRetry(context);
        if (needRetry && updateRetryCount(context)) {
            Job job = context.getJob();
            JobTask jobTask = context.getJobTask();
            RealJobExecutorDTO realJobExecutor = JobTaskConverter.INSTANCE.toRealJobExecutorDTO(
                    JobTaskConverter.INSTANCE.toJobExecutorContext(job), jobTask);
            realJobExecutor.setClientId(ClientInfoUtils.clientId(context.getClientInfo()));
            realJobExecutor.setWorkflowNodeId(context.getWorkflowNodeId());
            realJobExecutor.setWorkflowTaskBatchId(context.getWorkflowTaskBatchId());
            realJobExecutor.setRetryCount(jobTask.getRetryCount() + 1);
            realJobExecutor.setRetry(Boolean.TRUE);
            realJobExecutor.setRetryScene(context.getRetryScene());
            realJobExecutor.setTaskName(jobTask.getTaskName());
            // 这里统一收口传递上下文
            if (StrUtil.isBlank(realJobExecutor.getWfContext())) {
                realJobExecutor.setWfContext(getWfContext(realJobExecutor.getWorkflowTaskBatchId()));
            }
            if (JobRetrySceneEnum.MANUAL.getRetryScene().equals(context.getRetryScene())) {
                // 手动重试, 则即时重试
                ActorRef actorRef = ActorGenerator.jobRealTaskExecutorActor();
                actorRef.tell(realJobExecutor, actorRef);
            } else {
                // 注册重试任务，重试间隔时间轮
                JobTimerWheel.registerWithJob(() -> new RetryJobTimerTask(realJobExecutor), Duration.ofSeconds(job.getRetryInterval()));
                return;
            }
        }

        // 不需要重试执行回调
        doCallback(context);
    }

    /**
     * 获取工作流批次
     *
     * @param workflowTaskBatchId 工作流批次
     * @return
     */
    private String getWfContext(Long workflowTaskBatchId) {
        if (Objects.isNull(workflowTaskBatchId)) {
            return null;
        }

        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectOne(
            new LambdaQueryWrapper<WorkflowTaskBatch>()
                .select(WorkflowTaskBatch::getWfContext)
                .eq(WorkflowTaskBatch::getId, workflowTaskBatchId)
        );

        if (Objects.isNull(workflowTaskBatch)) {
            return null;
        }

        return workflowTaskBatch.getWfContext();
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

        return SqlHelper.retBool(jobTaskMapper.update(updateJobTask, updateWrapper));

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
