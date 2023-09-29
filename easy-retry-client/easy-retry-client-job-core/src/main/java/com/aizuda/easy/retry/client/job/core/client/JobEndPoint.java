package com.aizuda.easy.retry.client.job.core.client;

import com.aizuda.easy.retry.client.job.core.IJobExecutor;
import com.aizuda.easy.retry.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.easy.retry.client.job.core.cache.ThreadPoolCache;
import com.aizuda.easy.retry.client.job.core.executor.AnnotationJobExecutor;
import com.aizuda.easy.retry.client.job.core.executor.NormalJobExecutor;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;
import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import com.aizuda.easy.retry.client.model.request.DispatchJobRequest;
import com.aizuda.easy.retry.client.model.StopJobDTO;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: www.byteblogs.com
 * @date : 2023-09-27 16:33
 */
@RestController
@RequestMapping("/job")
@Slf4j
public class JobEndPoint {

    @PostMapping("/dispatch/v1")
    public Result<Boolean> dispatchJob(@RequestBody @Validated DispatchJobRequest dispatchJob) {

        JobContext jobContext = new JobContext();
        jobContext.setJobId(dispatchJob.getJobId());
        jobContext.setTaskId(dispatchJob.getTaskId());
        jobContext.setTaskBatchId(dispatchJob.getTaskBatchId());
        jobContext.setGroupName(dispatchJob.getGroupName());
        jobContext.setExecutorName(dispatchJob.getExecutorName());
        jobContext.setParallelNum(dispatchJob.getParallelNum());
        jobContext.setTaskType(dispatchJob.getTaskType());
        jobContext.setExecutorTimeout(dispatchJob.getExecutorTimeout());

        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(jobContext.getExecutorName());
        if (jobExecutorInfo.isAnnotation()) {
            IJobExecutor iJobExecutor = SpringContext.getBeanByType(AnnotationJobExecutor.class);
            iJobExecutor.jobExecute(jobContext);
        } else {
            NormalJobExecutor normalJobExecutor = (NormalJobExecutor) jobExecutorInfo.getExecutor();
            normalJobExecutor.jobExecute(jobContext);
        }

        return new Result<>(Boolean.TRUE);
    }

    @PostMapping("/stop/v1")
    public Result<Boolean> stopJob(@RequestBody @Validated StopJobDTO interruptJob) {
        ThreadPoolExecutor threadPool = ThreadPoolCache.getThreadPool(interruptJob.getTaskId());
        if (Objects.isNull(threadPool) || threadPool.isShutdown() || threadPool.isTerminated()) {
            return new Result<>(Boolean.TRUE);
        }

        ThreadPoolCache.stopThreadPool(interruptJob.getTaskId());
        return new Result<>(threadPool.isShutdown() || threadPool.isTerminated());
    }
}
