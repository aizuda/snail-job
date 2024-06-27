package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.MapReduceStageEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.MapReduceArgsStrDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.*;

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

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected List<JobTask> doGenerate(final JobTaskGenerateContext context) {
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(),
                context.getNamespaceId());
        if (CollUtil.isEmpty(serverNodes)) {
            SnailJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        List<RegisterNodeInfo> nodeInfoList = new ArrayList<>(serverNodes);
        MapReduceStageEnum mapReduceStageEnum = MapReduceStageEnum.ofStage(context.getMrStage());
        Assert.notNull(mapReduceStageEnum, () -> new SnailJobServerException("Map reduce stage is not existed"));
        switch (Objects.requireNonNull(mapReduceStageEnum)) {
            case MAP -> {
                // MAP任务
                return createMapJobTasks(context, nodeInfoList, serverNodes);
            }
            case REDUCE -> {
                // REDUCE任务
                return createReduceJobTasks(context, nodeInfoList, serverNodes);
            }
            case MERGE_REDUCE -> {
                // REDUCE任务
                return createMergeReduceJobTasks(context, nodeInfoList, serverNodes);
            }
            default -> throw new SnailJobServerException("Map reduce stage is not existed");
        }
    }

    private List<JobTask> createMergeReduceJobTasks(JobTaskGenerateContext context, List<RegisterNodeInfo> nodeInfoList, Set<RegisterNodeInfo> serverNodes) {

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getResultMessage)
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
                .eq(JobTask::getMrStage, MapReduceStageEnum.REDUCE.getStage())
                .eq(JobTask::getLeaf, StatusEnum.YES.getStatus())
        );

        RegisterNodeInfo registerNodeInfo = nodeInfoList.get(0);
        // 新增任务实例
        JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
        jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
        jobTask.setArgsType(context.getArgsType());
        JobArgsHolder jobArgsHolder = new JobArgsHolder();
        jobArgsHolder.setJobParams(context.getArgsStr());
        jobArgsHolder.setReduces(JsonUtil.toJsonString(StreamUtils.toSet(jobTasks, JobTask::getResultMessage)));
        jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
        jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
        jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
        jobTask.setMrStage(MapReduceStageEnum.MERGE_REDUCE.getStage());
        jobTask.setTaskName(MERGE_REDUCE_TASK);
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask),
                () -> new SnailJobServerException("新增任务实例失败"));

        return Lists.newArrayList(jobTask);
    }

    private List<JobTask> createReduceJobTasks(JobTaskGenerateContext context, List<RegisterNodeInfo> nodeInfoList,
                                               Set<RegisterNodeInfo> serverNodes) {

        int reduceParallel = 1;
        String jobParams = null;
        try {
            MapReduceArgsStrDTO mapReduceArgsStrDTO = JsonUtil.parseObject(context.getArgsStr(), MapReduceArgsStrDTO.class);
            reduceParallel = Optional.ofNullable(mapReduceArgsStrDTO.getShardNum()).orElse(1);
            jobParams = mapReduceArgsStrDTO.getArgsStr();
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("map reduce args parse error. argsStr:[{}]", context.getArgsStr());
        }

        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getResultMessage)
                .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
                .eq(JobTask::getMrStage, MapReduceStageEnum.MAP.getStage())
                .eq(JobTask::getLeaf, StatusEnum.YES.getStatus())
        );

        // 这里需要判断是否是map
        List<String> allMapJobTasks = StreamUtils.toList(jobTasks, JobTask::getResultMessage);

        List<List<String>> partition = Lists.partition(allMapJobTasks, reduceParallel);

        jobTasks = new ArrayList<>(partition.size());
        final List<JobTask> finalJobTasks = jobTasks;
        String finalJobParams = jobParams;
        final List<JobTask> finalJobTasks1 = jobTasks;
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                for (int index = 0; index < partition.size(); index++) {
                    RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
                    // 新增任务实例
                    JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
                    jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
                    jobTask.setArgsType(context.getArgsType());
                    JobArgsHolder jobArgsHolder = new JobArgsHolder();
                    jobArgsHolder.setJobParams(finalJobParams);
                    jobArgsHolder.setMaps(partition.get(index));
                    jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
                    jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
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

                batchSaveJobTasks(finalJobTasks1);
            }
        });

        return finalJobTasks;
    }

    @NotNull
    private List<JobTask> createMapJobTasks(final JobTaskGenerateContext context,
                                            final List<RegisterNodeInfo> nodeInfoList, final Set<RegisterNodeInfo> serverNodes) {
        List<?> mapSubTask = context.getMapSubTask();
        if (CollUtil.isEmpty(mapSubTask)) {
            SnailJobLog.LOCAL.warn("Map sub task is empty. TaskBatchId:[{}]", context.getTaskBatchId());
            return Lists.newArrayList();
        }

        // 判定父节点是不是叶子节点，若是则不更新否则更新为非叶子节点
        List<JobTask> parentJobTasks = jobTaskMapper.selectList(new PageDTO<>(1, 1),
                new LambdaQueryWrapper<JobTask>().select(JobTask::getId)
                        .eq(JobTask::getId, context.getParentId())
                        .eq(JobTask::getLeaf, StatusEnum.YES.getStatus())
        );

        List<JobTask> jobTasks = new ArrayList<>(mapSubTask.size());

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                for (int index = 0; index < mapSubTask.size(); index++) {
                    RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
                    // 新增任务实例
                    JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
                    jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
                    jobTask.setArgsType(context.getArgsType());
                    JobArgsHolder jobArgsHolder = new JobArgsHolder();
                    jobArgsHolder.setJobParams(context.getArgsStr());
                    jobArgsHolder.setMaps(mapSubTask.get(index));
                    jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
                    jobTask.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
                    jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
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
                if (CollUtil.isNotEmpty(parentJobTasks)) {
                    JobTask parentJobTask = new JobTask();
                    parentJobTask.setId(context.getParentId());
                    parentJobTask.setLeaf(StatusEnum.NO.getStatus());
                    jobTaskMapper.updateById(parentJobTask);
                }

            }
        });


        return jobTasks;
    }
}