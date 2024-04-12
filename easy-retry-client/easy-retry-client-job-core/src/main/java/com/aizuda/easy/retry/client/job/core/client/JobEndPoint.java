package com.aizuda.easy.retry.client.job.core.client;

import com.aizuda.easy.retry.client.common.annotation.Mapping;
import com.aizuda.easy.retry.client.common.annotation.SnailEndPoint;
import com.aizuda.easy.retry.client.common.log.support.EasyRetryLogManager;
import com.aizuda.easy.retry.client.common.rpc.client.RequestMethod;
import com.aizuda.easy.retry.client.job.core.IJobExecutor;
import com.aizuda.easy.retry.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.easy.retry.client.job.core.cache.ThreadPoolCache;
import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import com.aizuda.easy.retry.client.job.core.executor.AbstractJobExecutor;
import com.aizuda.easy.retry.client.job.core.executor.AnnotationJobExecutor;
import com.aizuda.easy.retry.client.job.core.log.JobLogMeta;
import com.aizuda.easy.retry.client.model.StopJobDTO;
import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.model.JobContext;
import com.aizuda.easy.retry.common.core.model.Result;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: opensnail
 * @date : 2023-09-27 16:33
 */
@SnailEndPoint
public class JobEndPoint {

    @Mapping(path = "/job/dispatch/v1", method = RequestMethod.POST)
    public Result<Boolean> dispatchJob(DispatchJobRequest dispatchJob) {

        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<DispatchJobRequest>> set = validator.validate(dispatchJob);
        for (final ConstraintViolation<DispatchJobRequest> violation : set) {
            return new Result<>(violation.getMessage(), Boolean.FALSE);
        }

        try {
            JobContext jobContext = buildJobContext(dispatchJob);

            // 初始化调度信息（日志上报LogUtil）
            initLogContext(jobContext);

            if (Objects.nonNull(dispatchJob.getRetryCount()) && dispatchJob.getRetryCount() > 0) {
                EasyRetryLog.REMOTE.info("任务执行/调度失败执行重试. 重试次数:[{}]",
                    dispatchJob.getRetryCount());
            }

            JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(jobContext.getExecutorInfo());
            if (Objects.isNull(jobExecutorInfo)) {
                EasyRetryLog.REMOTE.error("执行器配置有误. executorInfo:[{}]", dispatchJob.getExecutorInfo());
                return new Result<>("执行器配置有误", Boolean.FALSE);
            }

            // 选择执行器
            Object executor = jobExecutorInfo.getExecutor();
            IJobExecutor jobExecutor;
            if (IJobExecutor.class.isAssignableFrom(executor.getClass())) {
                jobExecutor = (AbstractJobExecutor) executor;
            } else {
                jobExecutor = SpringContext.getBeanByType(AnnotationJobExecutor.class);
            }

            EasyRetryLog.REMOTE.info("批次:[{}] 任务调度成功. ", dispatchJob.getTaskBatchId());

            jobExecutor.jobExecute(jobContext);

        } catch (Exception e) {
            EasyRetryLog.REMOTE.error("客户端发生非预期异常. taskBatchId:[{}]", dispatchJob.getTaskBatchId());
            throw e;
        } finally {
            EasyRetryLogManager.removeLogMeta();
        }

        return new Result<>(Boolean.TRUE);
    }

    private void initLogContext(JobContext jobContext) {
        JobLogMeta logMeta = new JobLogMeta();
        logMeta.setNamespaceId(jobContext.getNamespaceId());
        logMeta.setTaskId(jobContext.getTaskId());
        logMeta.setGroupName(jobContext.getGroupName());
        logMeta.setJobId(jobContext.getJobId());
        logMeta.setTaskBatchId(jobContext.getTaskBatchId());
        EasyRetryLogManager.initLogInfo(logMeta, LogTypeEnum.JOB);
    }


    private static JobContext buildJobContext(DispatchJobRequest dispatchJob) {
        JobContext jobContext = new JobContext();
        jobContext.setJobId(dispatchJob.getJobId());
        jobContext.setNamespaceId(dispatchJob.getNamespaceId());
        jobContext.setTaskId(dispatchJob.getTaskId());
        jobContext.setTaskBatchId(dispatchJob.getTaskBatchId());
        jobContext.setGroupName(dispatchJob.getGroupName());
        jobContext.setExecutorInfo(dispatchJob.getExecutorInfo());
        jobContext.setParallelNum(dispatchJob.getParallelNum());
        jobContext.setTaskType(dispatchJob.getTaskType());
        jobContext.setExecutorTimeout(dispatchJob.getExecutorTimeout());
        jobContext.setArgsStr(dispatchJob.getArgsStr());
        jobContext.setWorkflowNodeId(dispatchJob.getWorkflowNodeId());
        jobContext.setWorkflowTaskBatchId(dispatchJob.getWorkflowTaskBatchId());
        jobContext.setRetry(dispatchJob.isRetry());
        jobContext.setRetryScene(dispatchJob.getRetryScene());
        return jobContext;
    }

    @Mapping(path = "/stop/v1", method = RequestMethod.POST)
    public Result<Boolean> stopJob(StopJobDTO interruptJob) {

        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<StopJobDTO>> set = validator.validate(interruptJob);
        for (final ConstraintViolation<StopJobDTO> violation : set) {
            return new Result<>(violation.getMessage(), Boolean.FALSE);
        }

        ThreadPoolExecutor threadPool = ThreadPoolCache.getThreadPool(interruptJob.getTaskBatchId());
        if (Objects.isNull(threadPool) || threadPool.isShutdown() || threadPool.isTerminated()) {
            return new Result<>(Boolean.TRUE);
        }

        ThreadPoolCache.stopThreadPool(interruptJob.getTaskBatchId());
        return new Result<>(threadPool.isShutdown() || threadPool.isTerminated());
    }
}
