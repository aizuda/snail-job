package com.aizuda.snailjob.client.job.core.executor;

import com.aizuda.snailjob.client.common.rpc.client.RequestBuilder;
import com.aizuda.snailjob.client.job.core.IJobExecutor;
import com.aizuda.snailjob.client.job.core.client.JobNettyClient;
import com.aizuda.snailjob.client.model.request.MapTaskRequest;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import com.aizuda.snailjob.common.core.model.JobContext;
import com.aizuda.snailjob.common.core.model.NettyResult;
import com.aizuda.snailjob.common.core.model.Result;
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
            .build();

    public void doMapExecute(JobContext jobContext) {

        List<Object> taskList = jobContext.getTaskList();
        String mapName = jobContext.getMapName();
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        // mapName 任务命名和根任务名或者最终任务名称一致导致的问题（无限生成子任务或者直接失败）
        if ("ROOT_TASK".equals(mapName) || "ROOT_TASK".equals(mapName)) {
            log.warn("[Map-{}] illegal map task name : {}! please do not use 'OMS_ROOT_TASK' or 'OMS_LAST_TASK' as map task name. as a precaution, it will be renamed 'X-{}' automatically.", jobContext.getTaskId(), mapName, mapName);
            mapName = "X-" + mapName;
        }

        // 1. 构造请求
        MapTaskRequest mapTaskRequest = new MapTaskRequest();
        mapTaskRequest.setJobId(jobContext.getJobId());
        mapTaskRequest.setTaskBatchId(jobContext.getTaskBatchId());
        mapTaskRequest.setTaskName(mapName);
        mapTaskRequest.setSubTask(taskList);

        // 2. 可靠发送请求（任务不允许丢失，需要使用 ask 方法，失败抛异常）
        Result<Boolean> booleanResult = CLIENT.batchReportMapTask(mapTaskRequest);

        if (booleanResult.getData()) {
            log.info("[Map-{}] map task[name={},num={}] successfully!", jobContext.getTaskId(), mapName, taskList.size());
        } else {
            throw new SnailJobMapReduceException("map failed for task: " + mapName);
        }
    }
}
