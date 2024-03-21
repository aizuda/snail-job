package com.aizuda.easy.retry.client.job.core.executor;

import com.aizuda.easy.retry.client.common.util.ThreadLocalLogUtil;
import com.aizuda.easy.retry.client.job.core.IJobExecutor;
import com.aizuda.easy.retry.client.job.core.cache.FutureCache;
import com.aizuda.easy.retry.client.job.core.cache.ThreadPoolCache;
import com.aizuda.easy.retry.client.job.core.dto.JobArgs;
import com.aizuda.easy.retry.client.job.core.dto.ShardingJobArgs;
import com.aizuda.easy.retry.client.job.core.log.JobLogMeta;
import com.aizuda.easy.retry.client.job.core.timer.StopTaskTimerTask;
import com.aizuda.easy.retry.client.job.core.timer.TimerManager;
import com.aizuda.easy.retry.client.model.ExecuteResult;
import com.aizuda.easy.retry.common.core.enums.JobTaskTypeEnum;
import com.aizuda.easy.retry.common.core.model.JobContext;
import com.aizuda.easy.retry.common.log.enums.LogTypeEnum;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 广播模式
 *
 * @author: www.byteblogs.com
 * @date : 2023-09-27 09:48
 * @since 2.4.0
 */
@Slf4j
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
            if (jobContext.getTaskType() == JobTaskTypeEnum.SHARDING.getType()) {
                jobArgs = buildShardingJobArgs(jobContext);
            } else {
                jobArgs = buildJobArgs(jobContext);
            }

            try {
                // 初始化调度信息（日志上报LogUtil）
                initLogContext(jobContext);
                return doJobExecute(jobArgs);
            } finally {
                ThreadLocalLogUtil.removeContext();
            }

        });

        FutureCache.addFuture(jobContext.getTaskBatchId(), submit);
        Futures.addCallback(submit, new JobExecutorFutureCallback(jobContext), decorator);
    }

    private void initLogContext(JobContext jobContext) {
        JobLogMeta logMeta = new JobLogMeta();
        logMeta.setNamespaceId(jobContext.getNamespaceId());
        logMeta.setTaskId(jobContext.getTaskId());
        logMeta.setGroupName(jobContext.getGroupName());
        logMeta.setJobId(jobContext.getJobId());
        logMeta.setTaskBatchId(jobContext.getTaskBatchId());
        ThreadLocalLogUtil.initLogInfo(logMeta, LogTypeEnum.JOB);
    }

    private static JobArgs buildJobArgs(JobContext jobContext) {
        JobArgs jobArgs = new JobArgs();
        jobArgs.setArgsStr(jobContext.getArgsStr());
        jobArgs.setExecutorInfo(jobContext.getExecutorInfo());
        jobArgs.setTaskBatchId(jobContext.getTaskBatchId());
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
