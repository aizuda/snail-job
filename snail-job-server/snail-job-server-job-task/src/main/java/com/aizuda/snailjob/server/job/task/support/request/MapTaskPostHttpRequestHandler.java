package com.aizuda.snailjob.server.job.task.support.request;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import com.aizuda.snailjob.client.model.request.MapTaskRequest;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.SnailJobRequest;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.PostHttpRequestHandler;
import com.aizuda.snailjob.server.common.util.HttpHeaderUtil;
import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import com.aizuda.snailjob.server.job.task.support.JobExecutor;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorContext;
import com.aizuda.snailjob.server.job.task.support.executor.job.JobExecutorFactory;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGenerator;
import com.aizuda.snailjob.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.aizuda.snailjob.server.job.task.support.handler.JobTaskBatchHandler;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;


/**
 * 动态分片客户端生成map任务
 *
 * @author: opensnail
 * @date : 2024-06-12
 * @since : sj_1.1.0
 */
@Component
@RequiredArgsConstructor
public  class MapTaskPostHttpRequestHandler extends PostHttpRequestHandler {
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final JobMapper jobMapper;
    private final JobTaskBatchHandler jobTaskBatchHandler;

    @Override
    public boolean supports(final String path) {
        return SystemConstants.HTTP_PATH.BATCH_REPORT_JOB_MAP_TASK.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SnailJobRpcResult doHandler(final String content, final UrlQuery query, final HttpHeaders headers) {
        SnailJobLog.LOCAL.info("map task Request. content:[{}]", content);
        String groupName = HttpHeaderUtil.getGroupName(headers);
        String namespace = HttpHeaderUtil.getNamespace(headers);

        SnailJobRequest retryRequest = JsonUtil.parseObject(content, SnailJobRequest.class);

        try {
            return doHandlerMapTask(retryRequest, namespace, groupName);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("map task Request error. content:[{}]", content, e);
            return new SnailJobRpcResult(StatusEnum.NO.getStatus(), e.getMessage(), Boolean.FALSE,
                    retryRequest.getReqId());
        }

    }

    private SnailJobRpcResult doHandlerMapTask(SnailJobRequest retryRequest, String namespace, String groupName) {
        Object[] args = retryRequest.getArgs();
        MapTaskRequest mapTaskRequest = JsonUtil.parseObject(JsonUtil.toJsonString(args[0]), MapTaskRequest.class);

        Job job = jobMapper.selectOne(new LambdaQueryWrapper<Job>()
                .eq(Job::getId, mapTaskRequest.getJobId())
                .eq(Job::getGroupName, groupName)
                .eq(Job::getNamespaceId, namespace)
        );

        if (Objects.isNull(job)) {
            return new SnailJobRpcResult(StatusEnum.NO.getStatus(), "Job config not existed", Boolean.FALSE,
                    retryRequest.getReqId());
        }

        String argStr = jobTaskBatchHandler.getArgStr(mapTaskRequest.getTaskBatchId(), job);

        // 创建map任务
        JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(job.getTaskType());
        JobTaskGenerateContext context = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(mapTaskRequest);
        context.setGroupName(groupName);
        context.setArgsStr(argStr);
        context.setNamespaceId(namespace);
        context.setMrStage(MapReduceStageEnum.MAP.getStage());
        context.setMapSubTask(mapTaskRequest.getSubTask());
        context.setWfContext(mapTaskRequest.getWfContext());
        List<JobTask> taskList = taskInstance.generate(context);
        if (CollUtil.isEmpty(taskList)) {
            return new SnailJobRpcResult(StatusEnum.NO.getStatus(), "Job task is empty", Boolean.FALSE,
                    retryRequest.getReqId());
        }

        String newWfContext = null;
        if (Objects.nonNull(mapTaskRequest.getWorkflowTaskBatchId()) && mapTaskRequest.getWorkflowTaskBatchId() > 0) {
            WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchMapper.selectOne(
                new LambdaQueryWrapper<WorkflowTaskBatch>()
                    .select(WorkflowTaskBatch::getWfContext, WorkflowTaskBatch::getId)
                    .eq(WorkflowTaskBatch::getId, mapTaskRequest.getWorkflowTaskBatchId())
            );
            Assert.notNull(workflowTaskBatch, ()-> new SnailJobServerException("workflowTaskBatch is null. id:[{}]", mapTaskRequest.getWorkflowTaskBatchId()));
            newWfContext = workflowTaskBatch.getWfContext();
        }

        // 执行任务
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(JobTaskTypeEnum.MAP_REDUCE.getType());
        jobExecutor.execute(buildJobExecutorContext(mapTaskRequest, job, taskList, newWfContext));

        return new SnailJobRpcResult(StatusEnum.YES.getStatus(), "Report Map Task Processed Successfully", Boolean.TRUE,
                retryRequest.getReqId());
    }

    private static JobExecutorContext buildJobExecutorContext(MapTaskRequest mapTaskRequest, Job job,
        List<JobTask> taskList, String newWfContext) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(mapTaskRequest.getTaskBatchId());
        context.setWorkflowTaskBatchId(mapTaskRequest.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(mapTaskRequest.getWorkflowNodeId());
        context.setWfContext(newWfContext);
        return context;
    }

}
