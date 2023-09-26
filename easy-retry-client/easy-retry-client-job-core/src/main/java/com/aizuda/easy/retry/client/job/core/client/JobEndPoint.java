package com.aizuda.easy.retry.client.job.core.client;

import com.aizuda.easy.retry.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.easy.retry.client.job.core.cache.ThreadPoolCache;
import com.aizuda.easy.retry.client.job.core.dto.ExecuteResult;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;
import com.aizuda.easy.retry.client.job.core.dto.JobExecutorInfo;
import com.aizuda.easy.retry.client.model.DispatchJobDTO;
import com.aizuda.easy.retry.client.model.InterruptJobDTO;
import com.aizuda.easy.retry.common.core.context.SpringContext;
import com.aizuda.easy.retry.common.core.model.Result;
import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;
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
    public Result<Boolean> dispatchJob(@RequestBody @Validated DispatchJobDTO dispatchJob) {

        // 创建可执行的任务
        ThreadPoolExecutor threadPool = ThreadPoolCache.createThreadPool(dispatchJob.getTaskId(), dispatchJob.getParallelNum());

        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(threadPool);

        JobContext jobContext = new JobContext();
        jobContext.setJobId(dispatchJob.getJobId());
        jobContext.setTaskId(dispatchJob.getTaskId());
        jobContext.setGroupName(dispatchJob.getGroupName());

        String executorName = dispatchJob.getExecutorName();
        JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(executorName);

        // 执行任务
        ListenableFuture<ExecuteResult> submit = decorator.submit(() -> {
            return  (ExecuteResult) ReflectionUtils.invokeMethod(jobExecutorInfo.getMethod(), jobExecutorInfo.getExecutor(), jobContext);
        });

        Futures.addCallback(submit, new FutureCallback<ExecuteResult>() {
            @Override
            public void onSuccess(ExecuteResult result) {
                // 上报执行成功
            }

            @Override
            public void onFailure(Throwable t) {
               // 上报执行失败
            }
        }, threadPool);


        return new Result<>(Boolean.TRUE);
    }

    @PostMapping("/interrupt/v1")
    public Result<Boolean> dispatchJob(@RequestBody @Validated InterruptJobDTO interruptJob) {

        return new Result<>(Boolean.TRUE);
    }
}
