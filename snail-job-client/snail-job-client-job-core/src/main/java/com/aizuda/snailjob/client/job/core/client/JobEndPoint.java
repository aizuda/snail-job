package com.aizuda.snailjob.client.job.core.client;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.annotation.Mapping;
import com.aizuda.snailjob.client.common.annotation.SnailEndPoint;
import com.aizuda.snailjob.client.common.log.support.SnailJobLogManager;
import com.aizuda.snailjob.client.common.rpc.client.RequestMethod;
import com.aizuda.snailjob.client.job.core.IJobExecutor;
import com.aizuda.snailjob.client.job.core.cache.JobExecutorInfoCache;
import com.aizuda.snailjob.client.job.core.cache.ThreadPoolCache;
import com.aizuda.snailjob.client.job.core.dto.JobExecutorInfo;
import com.aizuda.snailjob.client.job.core.executor.*;
import com.aizuda.snailjob.client.job.core.log.JobLogMeta;
import com.aizuda.snailjob.client.model.StopJobDTO;
import com.aizuda.snailjob.client.model.request.DispatchJobRequest;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.ExecutorTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.model.JobContext;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.common.log.enums.LogTypeEnum;
import com.google.common.collect.Maps;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.JOB_DISPATCH;
import static com.aizuda.snailjob.common.core.constant.SystemConstants.HTTP_PATH.JOB_STOP;

/**
 * @author: opensnail
 * @date : 2023-09-27 16:33
 */
@SnailEndPoint
@Validated
public class JobEndPoint {

    @Mapping(path = JOB_DISPATCH, method = RequestMethod.POST)
    public Result<Boolean> dispatchJob(@Valid DispatchJobRequest dispatchJob) {

        try {
            JobContext jobContext = buildJobContext(dispatchJob);

            // 初始化调度信息（日志上报LogUtil）
            initLogContext(jobContext);

            if (Objects.nonNull(dispatchJob.getRetryCount()) && dispatchJob.getRetryCount() > 0) {
                SnailJobLog.REMOTE.info("任务执行/调度失败执行重试. 重试次数:[{}]",
                        dispatchJob.getRetryCount());
            }

            if (ExecutorTypeEnum.JAVA.getType() != dispatchJob.getExecutorType()) {
                SnailJobLog.REMOTE.error("不支持非Java类型的执行器. executorType:[{}]", dispatchJob.getExecutorType());
                return new Result<>("不支持非Java类型的执行器", Boolean.FALSE);
            }

            JobExecutorInfo jobExecutorInfo = JobExecutorInfoCache.get(jobContext.getExecutorInfo());
            if (Objects.isNull(jobExecutorInfo)) {
                SnailJobLog.REMOTE.error("执行器配置有误. executorInfo:[{}]", dispatchJob.getExecutorInfo());
                return new Result<>("执行器配置有误", Boolean.FALSE);
            }

            // 选择执行器
            Object executor = jobExecutorInfo.getExecutor();
            IJobExecutor jobExecutor;
            if (IJobExecutor.class.isAssignableFrom(executor.getClass())) {
                if (JobTaskTypeEnum.MAP.getType() == jobContext.getTaskType()) {
                    jobExecutor = (AbstractMapExecutor) executor;
                } else if (JobTaskTypeEnum.MAP_REDUCE.getType() == jobContext.getTaskType()) {
                    jobExecutor = (AbstractMapReduceExecutor) executor;
                } else {
                    jobExecutor = (AbstractJobExecutor) executor;
                }
            } else {
                if (JobTaskTypeEnum.MAP.getType() == jobContext.getTaskType()) {
                    jobExecutor = SnailSpringContext.getBeanByType(AnnotationMapJobExecutor.class);
                } else if (JobTaskTypeEnum.MAP_REDUCE.getType() == jobContext.getTaskType()) {
                    jobExecutor = SnailSpringContext.getBeanByType(AnnotationMapReduceJobExecutor.class);
                } else {
                    jobExecutor = SnailSpringContext.getBeanByType(AnnotationJobExecutor.class);
                }
            }

            SnailJobLog.REMOTE.info("任务调度方:[{}] 任务ID:[{}] 任务批次:[{}] 工作流批次:[{}] 任务调度成功. ",
                    Objects.isNull(dispatchJob.getWorkflowTaskBatchId()) ? "job" : "workflow",
                    dispatchJob.getJobId(),
                    dispatchJob.getTaskBatchId(),
                    dispatchJob.getWorkflowTaskBatchId());

            jobExecutor.jobExecute(jobContext);

        } catch (Exception e) {
            SnailJobLog.REMOTE.error("客户端发生非预期异常. taskBatchId:[{}]", dispatchJob.getTaskBatchId());
            throw e;
        } finally {
            SnailJobLogManager.removeLogMeta();
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
        SnailJobLogManager.initLogInfo(logMeta, LogTypeEnum.JOB);
    }


    private static JobContext buildJobContext(DispatchJobRequest dispatchJob) {
        JobContext jobContext = new JobContext();
        jobContext.setJobId(dispatchJob.getJobId());
        jobContext.setShardingTotal(dispatchJob.getShardingTotal());
        jobContext.setShardingIndex(dispatchJob.getShardingIndex());
        jobContext.setNamespaceId(dispatchJob.getNamespaceId());
        jobContext.setTaskId(dispatchJob.getTaskId());
        jobContext.setTaskBatchId(dispatchJob.getTaskBatchId());
        jobContext.setGroupName(dispatchJob.getGroupName());
        jobContext.setExecutorInfo(dispatchJob.getExecutorInfo());
        jobContext.setParallelNum(dispatchJob.getParallelNum());
        jobContext.setTaskType(dispatchJob.getTaskType());
        jobContext.setExecutorTimeout(dispatchJob.getExecutorTimeout());
        jobContext.setWorkflowNodeId(dispatchJob.getWorkflowNodeId());
        jobContext.setWorkflowTaskBatchId(dispatchJob.getWorkflowTaskBatchId());
        jobContext.setRetryStatus(dispatchJob.getRetryStatus());
        jobContext.setRetryScene(dispatchJob.getRetryScene());
        jobContext.setTaskName(dispatchJob.getTaskName());
        jobContext.setMrStage(dispatchJob.getMrStage());

        if (StrUtil.isNotBlank(dispatchJob.getArgsStr())) {
            try {
                jobContext.setJobArgsHolder(JsonUtil.parseObject(dispatchJob.getArgsStr(), JobArgsHolder.class));
            } catch (Exception e) {
                SnailJobLog.REMOTE.warn("argsStr parse error", e);
                JobArgsHolder jobArgsHolder = new JobArgsHolder();
                jobArgsHolder.setJobParams(dispatchJob.getArgsStr());
                jobContext.setJobArgsHolder(jobArgsHolder);
            }
        } else {
            // 没有数据给个空对象，方便后面取参数
            jobContext.setJobArgsHolder(new JobArgsHolder());
        }

        String wfContext = dispatchJob.getWfContext();
        if (StrUtil.isNotBlank(wfContext)) {
            try {
                jobContext.setWfContext(JsonUtil.parseConcurrentHashMap(wfContext));
            } catch (Exception e) {
                SnailJobLog.REMOTE.warn("workflow context parse error", e);
            }
        } else {
            jobContext.setWfContext(Maps.newConcurrentMap());
        }

        return jobContext;
    }

    @Mapping(path = JOB_STOP, method = RequestMethod.POST)
    public Result<Boolean> stopJob(@Valid StopJobDTO interruptJob) {

        ThreadPoolExecutor threadPool = ThreadPoolCache.getThreadPool(interruptJob.getTaskBatchId());
        if (Objects.isNull(threadPool) || threadPool.isShutdown() || threadPool.isTerminated()) {
            return new Result<>(Boolean.TRUE);
        }

        ThreadPoolCache.stopThreadPool(interruptJob.getTaskBatchId());
        return new Result<>(threadPool.isShutdown() || threadPool.isTerminated());
    }
}
