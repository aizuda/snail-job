package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskExtAttrsDTO;
import com.aizuda.snailjob.server.job.task.enums.MapReduceStageEnum;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    private final JobTaskMapper jobTaskMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.MAP_REDUCE;
    }

    @Override
    protected List<JobTask> doGenerate(final JobTaskGenerateContext context) {
        // TODO 若没有客户端节点JobTask是否需要创建????
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(),
            context.getNamespaceId());
        if (CollUtil.isEmpty(serverNodes)) {
            SnailJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        List<RegisterNodeInfo> nodeInfoList = new ArrayList<>(serverNodes);

        switch (context.getMrStage()) {
            case MAP -> {
                // MAP任务
                List<JobTask> newArrayList = createMapJobTasks(context, nodeInfoList, serverNodes);
                if (newArrayList != null) {
                    return newArrayList;
                }
            }
            case REDUCE -> {
                createReduceJobTasks(context, nodeInfoList, serverNodes);
            }
            default -> throw new SnailJobServerException("Map reduce stage is not existed");
        }

        return Lists.newArrayList();
    }

    private void createReduceJobTasks(JobTaskGenerateContext context, List<RegisterNodeInfo> nodeInfoList,
        Set<RegisterNodeInfo> serverNodes) {

        // TODO  reduce阶段的并行度
        int reduceParallel = 10;

        List<String> allMapJobTasks = StreamUtils.toList(jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
            .select(JobTask::getResultMessage)
            .eq(JobTask::getTaskBatchId, context.getTaskBatchId())
        ), JobTask::getResultMessage);

        List<List<String>> partition = Lists.partition(allMapJobTasks, reduceParallel);

        JobTaskExtAttrsDTO jobTaskExtAttrsDTO = new JobTaskExtAttrsDTO();
        jobTaskExtAttrsDTO.setMapName(context.getMapName());
        jobTaskExtAttrsDTO.setTaskType(JobTaskTypeEnum.MAP_REDUCE.getType());
        jobTaskExtAttrsDTO.setMrStage(MapReduceStageEnum.REDUCE.name());

        List<JobTask> jobTasks = new ArrayList<>(partition.size());
        for (int index = 0; index < partition.size(); index++) {
            RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
            // 新增任务实例
            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
            jobTask.setArgsType(context.getArgsType());
            jobTask.setArgsStr(JsonUtil.toJsonString(partition.get(index)));
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            jobTask.setExtAttrs(jobTaskExtAttrsDTO.toString());
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask),
                () -> new SnailJobServerException("新增任务实例失败"));
            jobTasks.add(jobTask);
        }
    }

    private @Nullable List<JobTask> createMapJobTasks(final JobTaskGenerateContext context,
        final List<RegisterNodeInfo> nodeInfoList, final Set<RegisterNodeInfo> serverNodes) {
        List<?> mapSubTask = context.getMapSubTask();
        if (CollUtil.isEmpty(mapSubTask)) {
            return Lists.newArrayList();
        }

        JobTaskExtAttrsDTO jobTaskExtAttrsDTO = new JobTaskExtAttrsDTO();
        jobTaskExtAttrsDTO.setMapName(context.getMapName());
        jobTaskExtAttrsDTO.setTaskType(JobTaskTypeEnum.MAP_REDUCE.getType());
        jobTaskExtAttrsDTO.setMrStage(MapReduceStageEnum.MAP.name());

        List<JobTask> jobTasks = new ArrayList<>(mapSubTask.size());
        for (int index = 0; index < mapSubTask.size(); index++) {
            RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
            // 新增任务实例
            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
            jobTask.setArgsType(context.getArgsType());
            jobTask.setArgsStr(JsonUtil.toJsonString(mapSubTask.get(index)));
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            jobTask.setExtAttrs(jobTaskExtAttrsDTO.toString());
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask),
                () -> new SnailJobServerException("新增任务实例失败"));
            jobTasks.add(jobTask);
        }

        return Lists.newArrayList();
    }
}
