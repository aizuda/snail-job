package com.aizuda.snailjob.client.job.core.executor;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.job.core.IJobExecutor;
import com.aizuda.snailjob.client.job.core.client.JobNettyClient;
import com.aizuda.snailjob.client.job.core.dto.JobArgs;
import com.aizuda.snailjob.client.job.core.dto.MapArgs;
import com.aizuda.snailjob.client.model.ExecuteResult;
import com.aizuda.snailjob.client.model.request.MapTaskRequest;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import com.aizuda.snailjob.common.core.model.JobContext;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.Result;
import com.aizuda.snailjob.common.log.SnailJobLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zhengweilin
 * @version 1.0.0
 * @date 2024/06/12
 */
@Slf4j
public abstract class AbstractMapExecutor extends AbstractJobExecutor implements IJobExecutor {

    private static final JobNettyClient CLIENT = RequestBuilder.<JobNettyClient, NettyResult>newBuilder()
            .client(JobNettyClient.class)
            .async(Boolean.FALSE)
            .build();

    @Override
    protected ExecuteResult doJobExecute(final JobArgs jobArgs) {
        MapArgs mapArgs = (MapArgs) jobArgs;
        return this.doJobMapExecute(mapArgs);
    }

    public abstract ExecuteResult doJobMapExecute(MapArgs mapArgs);

    public ExecuteResult doMap(List<Object> taskList, String nextTaskName) {

        if (StrUtil.isBlank(nextTaskName)) {
            throw new SnailJobMapReduceException("The next task name can not blank or null {}", nextTaskName);
        }

        if (CollectionUtils.isEmpty(taskList)) {
            throw new SnailJobMapReduceException("The task list can not empty {}", nextTaskName);
        }

        // taskName 任务命名和根任务名或者最终任务名称一致导致的问题（无限生成子任务或者直接失败）
        if (SystemConstants.MAP_ROOT.equals(nextTaskName)) {
            throw new SnailJobMapReduceException("The Next taskName can not be {}", SystemConstants.MAP_ROOT);
        }

        JobContext jobContext = JobContextManager.getJobContext();

        // 1. 构造请求
        MapTaskRequest mapTaskRequest = new MapTaskRequest();
        mapTaskRequest.setJobId(jobContext.getJobId());
        mapTaskRequest.setTaskBatchId(jobContext.getTaskBatchId());
        mapTaskRequest.setTaskName(nextTaskName);
        mapTaskRequest.setSubTask(taskList);
        mapTaskRequest.setParentId(jobContext.getTaskId());

        // 2. 同步发送请求
        Result<Boolean> result = CLIENT.batchReportMapTask(mapTaskRequest);
        if (result.getData()) {
            SnailJobLog.LOCAL.info("Map task create successfully!. taskName:[{}] TaskId:[{}] ", nextTaskName, jobContext.getTaskId());
        } else {
            throw new SnailJobMapReduceException("map failed for task: " + nextTaskName);
        }

        return ExecuteResult.success();
    }
}
