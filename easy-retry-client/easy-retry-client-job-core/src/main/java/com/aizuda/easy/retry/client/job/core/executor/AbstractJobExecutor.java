package com.aizuda.easy.retry.client.job.core.executor;

import com.aizuda.easy.retry.client.job.core.IJobExecutor;
import com.aizuda.easy.retry.client.job.core.cache.FutureCache;
import com.aizuda.easy.retry.client.job.core.cache.ThreadPoolCache;
import com.aizuda.easy.retry.client.job.core.dto.JobArgs;
import com.aizuda.easy.retry.client.job.core.dto.JobContext;
import com.aizuda.easy.retry.client.job.core.dto.ShardingJobArgs;
import com.aizuda.easy.retry.client.job.core.timer.StopTaskTimerTask;
import com.aizuda.easy.retry.client.job.core.timer.TimerManager;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.common.core.enums.TaskTypeEnum;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 广播模式
 *
 * @author: www.byteblogs.com
 * @date : 2023-09-27 09:48
 * @since 2.4.0
 */
public abstract class AbstractJobExecutor implements IJobExecutor {

    @Override
    public void jobExecute(JobContext jobContext) {

        // 创建可执行的任务
        ThreadPoolExecutor threadPool = ThreadPoolCache.createThreadPool(jobContext.getTaskBatchId(), jobContext.getParallelNum());
        ListeningExecutorService decorator = MoreExecutors.listeningDecorator(threadPool);

        // 将任务添加到时间轮中，到期停止任务
        TimerManager.add(new StopTaskTimerTask(jobContext.getTaskBatchId()), jobContext.getExecutorTimeout(), TimeUnit.SECONDS);

        // 执行任务
        ListenableFuture<ExecuteResult> submit = decorator.submit(() -> {
            JobArgs jobArgs;
            if (jobContext.getTaskType() == TaskTypeEnum.SHARDING.getType()) {
                jobArgs = buildShardingJobArgs(jobContext);
            } else {
                jobArgs = buildJobArgs(jobContext);
            }

            return doJobExecute(jobArgs);
        });

        FutureCache.addFuture(jobContext.getTaskBatchId(), submit);
        Futures.addCallback(submit, new JobExecutorFutureCallback(jobContext), decorator);
    }

    private static JobArgs buildJobArgs(JobContext jobContext) {
        JobArgs jobArgs = new JobArgs();
        jobArgs.setArgsStr(jobContext.getArgsStr());
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        return jobArgs;
    }

    private static JobArgs buildShardingJobArgs(JobContext jobContext) {
        ShardingJobArgs jobArgs = new ShardingJobArgs();
        jobArgs.setArgsStr(jobContext.getArgsStr());
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setShardingIndex(jobContext.getShardingIndex());
        jobArgs.setShardingTotal(jobContext.getShardingTotal());
        return jobArgs;
    }

    protected abstract ExecuteResult doJobExecute(JobArgs jobArgs);

}
