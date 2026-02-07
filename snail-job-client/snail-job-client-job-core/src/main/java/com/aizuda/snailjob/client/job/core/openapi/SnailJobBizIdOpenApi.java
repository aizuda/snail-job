package com.aizuda.snailjob.client.job.core.openapi;

import com.aizuda.snailjob.client.job.core.handler.add.*;
import com.aizuda.snailjob.client.job.core.handler.delete.DeleteJobBizIdHandler;
import com.aizuda.snailjob.client.job.core.handler.delete.DeleteWorkflowBizIdsHandler;
import com.aizuda.snailjob.client.job.core.handler.query.RequestQueryBizIdHandler;
import com.aizuda.snailjob.client.job.core.handler.trigger.*;
import com.aizuda.snailjob.client.job.core.handler.update.*;

import java.util.Set;

/**
 * BizId-based OpenAPI for SnailJob client operations.
 * Provides the same functionality as SnailJobOpenApi but uses bizId (String)
 * instead of jobId (Long) for task identification.
 *
 * @author opensnail
 * @since sj_1.2.0
 */
public final class SnailJobBizIdOpenApi {

    private SnailJobBizIdOpenApi() {
    }

    /**
     * 添加集群定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link ClusterAddBizIdHandler}
     */
    public static ClusterAddBizIdHandler addClusterJobByBizId(String bizId) {
        return new ClusterAddBizIdHandler(bizId);
    }

    /**
     * 添加广播定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link BroadcastAddBizIdHandler}
     */
    public static BroadcastAddBizIdHandler addBroadcastJobByBizId(String bizId) {
        return new BroadcastAddBizIdHandler(bizId);
    }

    /**
     * 添加固定分片定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link ShardingAddBizIdHandler}
     */
    public static ShardingAddBizIdHandler addShardingJobByBizId(String bizId) {
        return new ShardingAddBizIdHandler(bizId);
    }

    /**
     * 添加Map定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link MapAddBizIdHandler}
     */
    public static MapAddBizIdHandler addMapJobByBizId(String bizId) {
        return new MapAddBizIdHandler(bizId);
    }

    /**
     * 添加MapReduce定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link MapReduceAddBizIdHandler}
     */
    public static MapReduceAddBizIdHandler addMapReduceJobByBizId(String bizId) {
        return new MapReduceAddBizIdHandler(bizId);
    }

    /**
     * 更新集群定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link ClusterUpdateBizIdHandler}
     */
    public static ClusterUpdateBizIdHandler updateClusterJobByBizId(String bizId) {
        return new ClusterUpdateBizIdHandler(bizId);
    }

    /**
     * 更新广播定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link BroadcastUpdateBizIdHandler}
     */
    public static BroadcastUpdateBizIdHandler updateBroadcastJobByBizId(String bizId) {
        return new BroadcastUpdateBizIdHandler(bizId);
    }

    /**
     * 更新固定分片定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link ShardingUpdateBizIdHandler}
     */
    public static ShardingUpdateBizIdHandler updateShardingJobByBizId(String bizId) {
        return new ShardingUpdateBizIdHandler(bizId);
    }

    /**
     * 更新Map定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link MapUpdateBizIdHandler}
     */
    public static MapUpdateBizIdHandler updateMapJobByBizId(String bizId) {
        return new MapUpdateBizIdHandler(bizId);
    }

    /**
     * 更新MapReduce定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link MapReduceUpdateBizIdHandler}
     */
    public static MapReduceUpdateBizIdHandler updateMapReduceJobByBizId(String bizId) {
        return new MapReduceUpdateBizIdHandler(bizId);
    }

    /**
     * 获取定时任务详情
     *
     * @param bizId 业务唯一标识
     * @return {@link RequestQueryBizIdHandler}
     */
    public static RequestQueryBizIdHandler getJobDetailByBizId(String bizId) {
        return new RequestQueryBizIdHandler(bizId);
    }

    /**
     * 手动触发集群定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link ClusterTriggerBizIdHandler}
     */
    public static ClusterTriggerBizIdHandler triggerClusterJobByBizId(String bizId) {
        return new ClusterTriggerBizIdHandler(bizId);
    }

    /**
     * 手动触发广播定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link BroadcastTriggerBizIdHandler}
     */
    public static BroadcastTriggerBizIdHandler triggerBroadcastJobByBizId(String bizId) {
        return new BroadcastTriggerBizIdHandler(bizId);
    }

    /**
     * 手动触发固定分片定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link ShardingTriggerBizIdHandler}
     */
    public static ShardingTriggerBizIdHandler triggerShardingJobByBizId(String bizId) {
        return new ShardingTriggerBizIdHandler(bizId);
    }

    /**
     * 手动触发Map定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link MapTriggerBizIdHandler}
     */
    public static MapTriggerBizIdHandler triggerMapJobByBizId(String bizId) {
        return new MapTriggerBizIdHandler(bizId);
    }

    /**
     * 手动触发MapReduce定时任务
     *
     * @param bizId 业务唯一标识
     * @return {@link MapReduceTriggerBizIdHandler}
     */
    public static MapReduceTriggerBizIdHandler triggerMapReduceJobByBizId(String bizId) {
        return new MapReduceTriggerBizIdHandler(bizId);
    }

    /**
     * 更新定时任务状态
     *
     * @param bizId 业务唯一标识
     * @return {@link UpdateJobStatusBizIdHandler}
     */
    public static UpdateJobStatusBizIdHandler updateJobStatusByBizId(String bizId) {
        return new UpdateJobStatusBizIdHandler(bizId);
    }

    /**
     * 删除定时任务
     *
     * @param bizIds 待删除任务BizId集合
     * @return {@link DeleteJobBizIdHandler}
     */
    public static DeleteJobBizIdHandler deleteJobByBizIds(Set<String> bizIds) {
        return new DeleteJobBizIdHandler(bizIds);
    }

    /**
     * 手动触发工作流任务
     *
     * @param bizId 业务唯一标识
     * @return {@link TriggerWorkflowBizIdHandler}
     */
    public static TriggerWorkflowBizIdHandler triggerWorkflowByBizId(String bizId) {
        return new TriggerWorkflowBizIdHandler(bizId);
    }

    /**
     * 更新工作流任务状态
     *
     * @param bizId 业务唯一标识
     * @return {@link UpdateWorkflowStatusBizIdHandler}
     */
    public static UpdateWorkflowStatusBizIdHandler updateWorkflowStatusByBizId(String bizId) {
        return new UpdateWorkflowStatusBizIdHandler(bizId);
    }

    /**
     * 删除工作流任务
     *
     * @param bizIds 待删除工作流BizId集合
     * @return {@link DeleteWorkflowBizIdsHandler}
     */
    public static DeleteWorkflowBizIdsHandler deleteWorkflowByBizIds(Set<String> bizIds) {
        return new DeleteWorkflowBizIdsHandler(bizIds);
    }

}