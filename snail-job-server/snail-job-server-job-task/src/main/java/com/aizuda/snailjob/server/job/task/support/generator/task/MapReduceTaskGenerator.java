package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.*;
import com.aizuda.snailjob.common.core.exception.SnailJobMapReduceException;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.allocate.client.ClientLoadBalanceManager;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.MapReduceArgsStrDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 生成Map Reduce任务
 *
 * @author: opensnail
 * @date : 2024-06-12
 * @since : sj_1.1.0
 */
@Component
@RequiredArgsConstructor
public class MapReduceTaskGenerator extends AbstractJobTaskGenerator {

    private static final String MERGE_REDUCE_TASK = "MERGE_REDUCE_TASK";
    private static final String REDUCE_TASK = "REDUCE_TASK";
    private final JobTaskMapper jobTaskMapper;
    private final TransactionTemplate transactionTemplate;
    private final ClientNodeAllocateHandler clientNodeAllocateHandler;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected List<JobTask> doGenerate(final JobTaskGenerateContext context) {
        MapReduceStageEnum mapReduceStageEnum = MapReduceStageEnum.ofStage(context.getMrStage());
        Assert.notNull(mapReduceStageEnum, () -> new SnailJobServerException("Map reduce stage is not existed"));
        switch (Objects.requireNonNull(mapReduceStageEnum)) {
            case MAP -> {
                // MAP任务
                return createMapJobTasks(context);
            }
            case REDUCE -> {
                // REDUCE任务
                return createReduceJobTasks(context);
            }
            case MERGE_REDUCE -> {
                // REDUCE任务
                return createMergeReduceJobTasks(context);
            }
            default -> throw new SnailJobServerException("Map reduce stage is not existed");
        }
    }

    private List<JobTask> createMergeReduceJobTasks(JobTaskGenerateContext context) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getResultMessage)
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
                .eq(JobTask::getMrStage, MapReduceStageEnum.REDUCE.getStage())
                .eq(JobTask::getLeaf, StatusEnum.YES.getStatus())
        );

        MapReduceArgsStrDTO jobParams = getJobParams(context);
        Pair<String, Integer> clientInfo = getClientNodeInfo(context);
        // 新增任务实例
        JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
        jobTask.setClientInfo(clientInfo.getKey());
        jobTask.setArgsType(context.getArgsType());
        JobArgsHolder jobArgsHolder = new JobArgsHolder();
        jobArgsHolder.setJobParams(jobParams.getArgsStr());
        jobArgsHolder.setReduces(StreamUtils.toList(jobTasks, JobTask::getResultMessage));
        jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
        jobTask.setTaskStatus(clientInfo.getValue());
        jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
        jobTask.setMrStage(MapReduceStageEnum.MERGE_REDUCE.getStage());
        jobTask.setTaskName(MERGE_REDUCE_TASK);
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask),
                () -> new SnailJobServerException("Adding new task instance failed"));

        return Lists.newArrayList(jobTask);
    }

    private List<JobTask> createReduceJobTasks(JobTaskGenerateContext context) {

        MapReduceArgsStrDTO jobParams = getJobParams(context);
        int reduceParallel = Math.max(1,
                Optional.ofNullable(jobParams.getShardNum()).orElse(1));

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getResultMessage, JobTask::getId)
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
                .eq(JobTask::getMrStage, MapReduceStageEnum.MAP.getStage())
                .eq(JobTask::getLeaf, StatusEnum.YES.getStatus())
        );

        if (CollUtil.isEmpty(jobTasks)) {
            return Lists.newArrayList();
        }

        // 这里需要判断是否是map
        List<String> allMapJobTasks = StreamUtils.toList(jobTasks, JobTask::getResultMessage);
        List<List<String>> partition = averageAlgorithm(allMapJobTasks, reduceParallel);

        jobTasks = new ArrayList<>(partition.size());
        final List<JobTask> finalJobTasks = jobTasks;
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                for (int index = 0; index < partition.size(); index++) {
                    Pair<String, Integer> clientInfo = getClientNodeInfo(context);

                    // 新增任务实例
                    JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
                    jobTask.setClientInfo(clientInfo.getKey());
                    jobTask.setArgsType(context.getArgsType());
                    JobArgsHolder jobArgsHolder = new JobArgsHolder();
                    jobArgsHolder.setJobParams(jobParams.getArgsStr());
                    jobArgsHolder.setMaps(partition.get(index));
                    jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
                    jobTask.setTaskStatus(clientInfo.getValue());
                    jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
                    jobTask.setMrStage(MapReduceStageEnum.REDUCE.getStage());
                    jobTask.setTaskName(REDUCE_TASK);
                    jobTask.setParentId(0L);
                    jobTask.setRetryCount(0);
                    jobTask.setLeaf(StatusEnum.YES.getStatus());
                    jobTask.setCreateDt(LocalDateTime.now());
                    jobTask.setUpdateDt(LocalDateTime.now());
                    finalJobTasks.add(jobTask);
                }

                batchSaveJobTasks(finalJobTasks);
            }
        });

        return finalJobTasks;
    }

    private List<JobTask> createMapJobTasks(final JobTaskGenerateContext context) {
        List<?> mapSubTask = context.getMapSubTask();
        if (CollUtil.isEmpty(mapSubTask)) {
            SnailJobLog.LOCAL.warn("Map sub task is empty. TaskBatchId:[{}]", context.getTaskBatchId());
            return Lists.newArrayList();
        }

        MapReduceArgsStrDTO jobParams = getJobParams(context);

        // 判定父节点是不是叶子节点，若是则不更新否则更新为非叶子节点
        JobTask parentJobTask = jobTaskMapper.selectOne(
                new LambdaQueryWrapper<JobTask>()
                        .select(JobTask::getId)
                        .eq(JobTask::getId, Optional.ofNullable(context.getParentId()).orElse(0L))
                        .eq(JobTask::getLeaf, StatusEnum.YES.getStatus())
        );

        return transactionTemplate.execute(status -> {
            List<JobTask> jobTasks = new ArrayList<>(mapSubTask.size());
            for (int index = 0; index < mapSubTask.size(); index++) {
                Pair<String, Integer> clientInfo = getClientNodeInfo(context);

                // 新增任务实例
                JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
                jobTask.setClientInfo(clientInfo.getKey());
                jobTask.setArgsType(context.getArgsType());
                JobArgsHolder jobArgsHolder = new JobArgsHolder();
                jobArgsHolder.setJobParams(jobParams.getArgsStr());
                jobArgsHolder.setMaps(mapSubTask.get(index));
                jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
                jobTask.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
                jobTask.setTaskStatus(clientInfo.getValue());
                jobTask.setMrStage(MapReduceStageEnum.MAP.getStage());
                jobTask.setTaskName(context.getTaskName());
                jobTask.setLeaf(StatusEnum.YES.getStatus());
                jobTask.setParentId(Objects.isNull(context.getParentId()) ? 0L : context.getParentId());
                jobTask.setRetryCount(0);
                jobTask.setCreateDt(LocalDateTime.now());
                jobTask.setUpdateDt(LocalDateTime.now());
                jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
                jobTasks.add(jobTask);
            }

            batchSaveJobTasks(jobTasks);

            // 更新父节点的为非叶子节点
            if (Objects.nonNull(parentJobTask)) {
                JobTask parentJobTask1 = new JobTask();
                parentJobTask1.setId(context.getParentId());
                parentJobTask1.setLeaf(StatusEnum.NO.getStatus());
                Assert.isTrue(1 == jobTaskMapper.updateById(parentJobTask1),
                        () -> new SnailJobMapReduceException("Updating parent node failed"));
            }

            return jobTasks;
        });

    }

    protected MapReduceArgsStrDTO getJobParams(JobTaskGenerateContext context) {
        try {
            return JsonUtil.parseObject(context.getArgsStr(), MapReduceArgsStrDTO.class);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("map reduce args parse error. argsStr:[{}]", context.getArgsStr());
        }

        return new MapReduceArgsStrDTO();
    }

    private Pair<String, Integer> getClientNodeInfo(JobTaskGenerateContext context) {
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(
                context.getJobId().toString(),
                context.getGroupName(),
                context.getNamespaceId(),
                ClientLoadBalanceManager.AllocationAlgorithmEnum.ROUND.getType()
        );
        String clientInfo = StrUtil.EMPTY;
        int JobTaskStatus = JobTaskStatusEnum.RUNNING.getStatus();
        if (Objects.isNull(serverNode)) {
            JobTaskStatus = JobTaskStatusEnum.CANCEL.getStatus();
        } else {
            clientInfo = ClientInfoUtils.generate(serverNode);
        }

        return Pair.of(clientInfo, JobTaskStatus);
    }

    private List<List<String>> averageAlgorithm(List<String> allMapJobTasks, int shard) {

        // 最多分片数为allMapJobTasks.size()
        shard = Math.min(allMapJobTasks.size(), shard);
        int totalSize = allMapJobTasks.size();
        List<Integer> partitionSizes = new ArrayList<>();
        int quotient = totalSize / shard;
        int remainder = totalSize % shard;

        for (int i = 0; i < shard; i++) {
            partitionSizes.add(quotient + (i < remainder ? 1 : 0));
        }

        List<List<String>> partitions = new ArrayList<>();
        int currentIndex = 0;

        for (int size : partitionSizes) {
            int endIndex = Math.min(currentIndex + size, totalSize);
            partitions.add(new ArrayList<>(allMapJobTasks.subList(currentIndex, endIndex)));
            currentIndex = endIndex;
        }

        return partitions;
    }
}
