package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.job.core.enums.JobTypeEnum;
import com.aizuda.snailjob.client.job.core.handler.add.*;
import com.aizuda.snailjob.client.job.core.handler.quert.RequestQueryHandler;
import com.aizuda.snailjob.client.job.core.handler.trigger.RequestTriggerJobHandler;
import com.aizuda.snailjob.client.job.core.handler.update.RequestUpdateHandler;
import com.aizuda.snailjob.client.job.core.handler.update.RequestUpdateStatusHandler;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;

/**
 * @author opensnail
 * @date 2024-09-21 21:35:34
 * @since sj_1.1.0
 */
public final class SnailJobOpenApi {
    private SnailJobOpenApi() {
    }

    /**
     * 添加集群定时任务
     *
     * @return {@link ClusterAddHandler}
     */
    public static ClusterAddHandler addClusterJob() {
        return new ClusterAddHandler(JobTaskTypeEnum.CLUSTER);
    }

    /**
     * 添加广播定时任务
     *
     * @return {@link BroadcastAddHandler}
     */
    public static BroadcastAddHandler addBroadcastJob() {
        return new BroadcastAddHandler();
    }

    /**
     * 添加固定分片定时任务
     *
     * @return {@link ShardingAddHandler}
     */
    public static ShardingAddHandler addShardingJob() {
        return new ShardingAddHandler();
    }

    /**
     * 添加Map定时任务
     *
     * @return {@link MapAddHandler}
     */
    public static MapAddHandler addMapJob() {
        return new MapAddHandler();
    }

    /**
     * 添加MapReduce定时任务
     *
     * @param shardNum Reduce数量
     * @return {@link MapReduceAddHandler}
     */
    public static MapReduceAddHandler addMapReduceJob(Integer shardNum) {
        return new MapReduceAddHandler(shardNum);
    }

    /**
     * 更新定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link RequestUpdateHandler}
     */
    public static RequestUpdateHandler updateJob(Long jobId) {
        return new RequestUpdateHandler(jobId);
    }

    /**
     * 获取定时任务详情
     *
     * @param jobId 定时任务ID
     * @return {@link RequestQueryHandler}
     */
    public static RequestQueryHandler getJobDetail(Long jobId) {
        return new RequestQueryHandler(jobId);
    }

    /**
     * 手动触发定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link RequestTriggerJobHandler}
     */
    public static RequestTriggerJobHandler triggerJob(Long jobId) {
        return new RequestTriggerJobHandler(jobId, JobTypeEnum.JOB.getType());
    }

    /**
     * 手动触发工作流任务
     *
     * @param id 工作流任务ID
     * @return {@link RequestTriggerJobHandler}
     */
    public static RequestTriggerJobHandler triggerWorkFlow(Long id) {
        return new RequestTriggerJobHandler(id, JobTypeEnum.WORKFLOW.getType());
    }

    /**
     * 更新定时任务状态
     *
     * @param jobId 任务ID
     * @return {@link RequestUpdateStatusHandler}
     */
    public static RequestUpdateStatusHandler updateJobStatus(Long jobId) {
        return new RequestUpdateStatusHandler(jobId, JobTypeEnum.JOB.getType());
    }

    /**
     * 更新工作流任务状态
     *
     * @param workFlowId 工作流ID
     * @return {@link RequestUpdateStatusHandler}
     */
    public static RequestUpdateStatusHandler updateWorkFlowStatus(Long workFlowId) {
        return new RequestUpdateStatusHandler(workFlowId, JobTypeEnum.WORKFLOW.getType());
    }
}
