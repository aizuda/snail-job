package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.job.core.handler.add.*;
import com.aizuda.snailjob.client.job.core.handler.delete.DeleteJobHandler;
import com.aizuda.snailjob.client.job.core.handler.delete.DeleteWorkflowHandler;
import com.aizuda.snailjob.client.job.core.handler.query.RequestQueryHandler;
import com.aizuda.snailjob.client.job.core.handler.query.RequestQueryJobBatchHandler;
import com.aizuda.snailjob.client.job.core.handler.query.RequestQueryWorkflowBatchHandler;
import com.aizuda.snailjob.client.job.core.handler.trigger.*;
import com.aizuda.snailjob.client.job.core.handler.update.*;

import java.util.Set;

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
        return new ClusterAddHandler();
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
     * @return {@link MapReduceAddHandler}
     */
    public static MapReduceAddHandler addMapReduceJob() {
        return new MapReduceAddHandler();
    }

    /**
     * 更新广播定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link BroadcastUpdateHandler}
     */
    public static BroadcastUpdateHandler updateBroadcastJob(Long jobId) {
        return new BroadcastUpdateHandler(jobId);
    }

    /**
     * 更新集群定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ClusterUpdateHandler}
     */
    public static ClusterUpdateHandler updateClusterJob(Long jobId) {
        return new ClusterUpdateHandler(jobId);
    }

    /**
     * 更新MapReduce定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapReduceUpdateHandler}
     */
    public static MapReduceUpdateHandler updateMapReduceJob(Long jobId) {
        return new MapReduceUpdateHandler(jobId);
    }

    /**
     * 更新Map定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapUpdateHandler}
     */
    public static MapUpdateHandler updateMapJob(Long jobId) {
        return new MapUpdateHandler(jobId);
    }

    /**
     * 更新静态分片定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ShardingUpdateHandler}
     */
    public static ShardingUpdateHandler updateShardingJob(Long jobId) {
        return new ShardingUpdateHandler(jobId);
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
     * 获取任务批次详情
     *
     * @param jobBatchId 任务批次 ID
     * @return {@link RequestQueryJobBatchHandler}
     */
    public static RequestQueryJobBatchHandler getJobBatchDetail(Long jobBatchId){
        return new RequestQueryJobBatchHandler(jobBatchId);
    }

    /**
     * 获取工作流任务批次详情
     *
     * @param workflowBatchId 工作流任务批次 ID
     * @return {@link RequestQueryWorkflowBatchHandler}
     */
    public static RequestQueryWorkflowBatchHandler getWorkflowBatchDetail(Long workflowBatchId){
        return new RequestQueryWorkflowBatchHandler(workflowBatchId);
    }

    /**
     * 手动触发广播定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link BroadcastTriggerHandler}
     */
    public static BroadcastTriggerHandler triggerBroadcastJob(Long jobId) {
        return new BroadcastTriggerHandler(jobId);
    }

    /**
     * 手动触发集群定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ClusterTriggerHandler}
     */
    public static ClusterTriggerHandler triggerClusterJob(Long jobId) {
        return new ClusterTriggerHandler(jobId);
    }

    /**
     * 手动触发MapReduce定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapReduceTriggerHandler}
     */
    public static MapReduceTriggerHandler triggerMapReduceJob(Long jobId) {
        return new MapReduceTriggerHandler(jobId);
    }

    /**
     * 手动触发Map定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link MapTriggerHandler}
     */
    public static MapTriggerHandler triggerMapJob(Long jobId) {
        return new MapTriggerHandler(jobId);
    }


    /**
     * 手动触发静态分片定时任务
     *
     * @param jobId 定时任务ID
     * @return {@link ShardingTriggerHandler}
     */
    public static ShardingTriggerHandler triggerShardingJob(Long jobId) {
        return new ShardingTriggerHandler(jobId);
    }



    /**
     * 手动触发工作流任务
     *
     * @param id 工作流任务ID
     * @return {@link TriggerWorkflowHandler}
     */
    public static TriggerWorkflowHandler triggerWorkFlow(Long id) {
        return new TriggerWorkflowHandler(id);
    }

    /**
     * 更新定时任务状态
     *
     * @param jobId 任务ID
     * @return {@link UpdateJobStatusHandler}
     */
    public static UpdateJobStatusHandler updateJobStatus(Long jobId) {
        return new UpdateJobStatusHandler(jobId);
    }

    /**
     * 更新工作流任务状态
     *
     * @param workFlowId 工作流ID
     * @return {@link UpdateJobStatusHandler}
     */
    public static UpdateWorkflowStatusHandler updateWorkFlowStatus(Long workFlowId) {
        return new UpdateWorkflowStatusHandler(workFlowId);
    }

    /**
     * 删除任务
     *
     * @param toDeleteIds 待删除任务IDS
     * @return {@link DeleteJobHandler}
     */
    public static DeleteJobHandler deleteJob(Set<Long> toDeleteIds){
        return new DeleteJobHandler(toDeleteIds);
    }

    /**
     * 删除工作流任务
     *
     * @param toDeleteIds 待删除工作流任务IDS
     * @return {@link DeleteWorkflowHandler}
     */
    public static DeleteWorkflowHandler deleteWorkflow(Set<Long> toDeleteIds){
        return new DeleteWorkflowHandler(toDeleteIds);
    }
}
