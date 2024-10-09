package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.job.core.handler.*;
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
     * @return
     */
    public static RequestAddHandler addClusterJob() {
        return new RequestAddHandler(JobTaskTypeEnum.CLUSTER, null);
    }

    /**
     * 添加广播定时任务
     *
     * @return
     */
    public static RequestAddHandler addBroadcastJob() {
        return new RequestAddHandler(JobTaskTypeEnum.BROADCAST, null);
    }

    /**
     * 添加固定分片定时任务
     *
     * @return
     */
    public static RequestAddHandler addShardingJob() {
        return new RequestAddHandler(JobTaskTypeEnum.SHARDING, null);
    }

    /**
     * 添加Map定时任务
     *
     * @return
     */
    public static RequestAddHandler addMapJob() {
        return new RequestAddHandler(JobTaskTypeEnum.MAP, null);
    }

    /**
     * 添加MapReduce定时任务
     *
     * @param shardNum Reduce数量
     * @return
     */
    public static RequestAddHandler addMapReduceJob(Integer shardNum) {
        return new RequestAddHandler(JobTaskTypeEnum.MAP_REDUCE, shardNum);
    }

    /**
     * 更新定时任务
     *
     * @param jobId 定时任务ID
     * @return
     */
    public static RequestUpdateHandler updateJob(Long jobId) {
        return new RequestUpdateHandler(jobId);
    }

    /**
     * 获取定时任务详情
     *
     * @param jobId 定时任务ID
     * @return
     */
    public static RequestQueryHandler getJobDetail(Long jobId) {
        return new RequestQueryHandler(jobId);
    }

    /**
     * 手动触发定时任务
     *
     * @param jobId 定时任务ID
     * @return
     */
    public static RequestTriggerJobHandler triggerJob(Long jobId) {
        return new RequestTriggerJobHandler(jobId, 1);
    }

    /**
     * 手动触发工作流任务
     *
     * @param id 工作流任务ID
     * @return
     */
    public static RequestTriggerJobHandler triggerWorkFlow(Long id) {
        return new RequestTriggerJobHandler(id, 2);
    }

    /**
     * 更新定时任务状态
     *
     * @param jobId 任务ID
     * @return
     */
    public static RequestUpdateStatusHandler updateJobStatus(Long jobId) {
        return new RequestUpdateStatusHandler(jobId, 1);
    }

    /**
     * 更新工作流任务状态
     *
     * @param workFlowId 工作流ID
     * @return
     */
    public static RequestUpdateStatusHandler updateWorkFlowStatus(Long workFlowId) {
        return new RequestUpdateStatusHandler(workFlowId, 2);
    }
}
