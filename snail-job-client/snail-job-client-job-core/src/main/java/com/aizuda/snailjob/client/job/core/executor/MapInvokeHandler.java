package com.aizuda.snailjob.client.job.core.executor;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.job.core.client.JobNettyClient;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.client.model.request.MapTaskRequest;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import com.aizuda.snailjob.common.core.model.JobContext;
import com.aizuda.snailjob.common.core.model.SnailJobRpcResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: opensnail
 * @date : 2024-06-26
 * @version : sj_1.1.0
 */
public final class MapInvokeHandler implements InvocationHandler {

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, SnailJobRpcResult>newBuilder()
        .client(JobNettyClient.class)
        .async(Boolean.FALSE)
        .build();

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return doMap((List<Object>) args[0], (String) args[1]);
    }

    public ExecuteResult doMap(List<Object> taskList, String nextTaskName) {

        if (StrUtil.isBlank(nextTaskName)) {
            throw new SnailJobMapReduceException("The next task name can not blank or null {}", nextTaskName);
        }

        if (CollectionUtils.isEmpty(taskList)) {
            throw new SnailJobMapReduceException("The task list can not empty {}", nextTaskName);
        }

        // 超过200提醒用户注意分片数量过多
        if (taskList.size() > 200) {
            SnailJobLog.LOCAL.warn("[{}] map task size is too large, network maybe overload... please try to split the tasks.", nextTaskName);
        }

        // 超过500强制禁止分片
        if (taskList.size() > 500) {
            throw new SnailJobMapReduceException("[{}] map task size is too large, network maybe overload... please try to split the tasks.", nextTaskName);
        }

        // taskName 任务命名和根任务名或者最终任务名称一致导致的问题（无限生成子任务或者直接失败）
        if (SystemConstants.ROOT_MAP.equals(nextTaskName)) {
            throw new SnailJobMapReduceException("The Next taskName can not be {}", SystemConstants.ROOT_MAP);
        }

        // 使用ThreadLocal传递数据
        JobContext jobContext = JobContextManager.getJobContext();
        Assert.notNull(jobContext, () -> new SnailJobMapReduceException("job context is null"));

        // 1. 构造请求
        MapTaskRequest mapTaskRequest = new MapTaskRequest();
        mapTaskRequest.setJobId(jobContext.getJobId());
        mapTaskRequest.setTaskBatchId(jobContext.getTaskBatchId());
        mapTaskRequest.setTaskName(nextTaskName);
        mapTaskRequest.setSubTask(taskList);
        mapTaskRequest.setParentId(jobContext.getTaskId());
        mapTaskRequest.setWorkflowTaskBatchId(jobContext.getWorkflowTaskBatchId());
        mapTaskRequest.setWorkflowNodeId(jobContext.getWorkflowNodeId());
        Map<String, Object> changeWfContext = jobContext.getChangeWfContext();
        if (Objects.nonNull(changeWfContext)) {
            mapTaskRequest.setWfContext(JsonUtil.toJsonString(changeWfContext));
        }

        // 2. 同步发送请求
        Result<Boolean> result = CLIENT.batchReportMapTask(mapTaskRequest);
        if (StatusEnum.YES.getStatus() == result.getStatus() || result.getData()) {
            SnailJobLog.LOCAL.info("Map task create successfully!. taskName:[{}] TaskId:[{}] ", nextTaskName, jobContext.getTaskId());
        } else {
            throw new SnailJobMapReduceException("map failed for task: " + nextTaskName);
        }

        return ExecuteResult.success();
    }


}
