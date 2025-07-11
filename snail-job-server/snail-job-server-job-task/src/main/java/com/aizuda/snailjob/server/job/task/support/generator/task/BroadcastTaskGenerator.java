package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author opensnail
 * @date 2023-10-02 21:25:08
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class BroadcastTaskGenerator extends AbstractJobTaskGenerator {
    private static final String TASK_NAME ="BROADCAST_TASK";
    private final InstanceManager instanceManager;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.BROADCAST;
    }

    @Override
    @Transactional
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {

        Set<InstanceLiveInfo> liveInfoSet = instanceManager.getInstanceALiveInfoSet(
                context.getNamespaceId(), context.getGroupName(), context.getLabels());
        if (CollUtil.isEmpty(liveInfoSet)) {
            SnailJobLog.LOCAL.error("No executable client information. Job ID:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        Set<String> clientInfoSet = new HashSet<>(liveInfoSet.size());
        List<JobTask> jobTasks = new ArrayList<>(liveInfoSet.size());
        for (InstanceLiveInfo liveInfo : liveInfoSet) {
            RegisterNodeInfo nodeInfo = liveInfo.getNodeInfo();
            // 若存在相同的IP信息则去重
            String address = nodeInfo.address();
            if (clientInfoSet.contains(address)) {
                continue;
            }

            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(nodeInfo));
            JobArgsHolder jobArgsHolder = new JobArgsHolder();
            jobArgsHolder.setJobParams(context.getArgsStr());
            jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
            jobTask.setArgsType(context.getArgsType());
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            jobTask.setParentId(0L);
            jobTask.setLeaf(StatusEnum.YES.getStatus());
            jobTask.setRetryCount(0);
            jobTask.setTaskName(TASK_NAME);
            jobTask.setCreateDt(LocalDateTime.now());
            jobTask.setUpdateDt(LocalDateTime.now());
            clientInfoSet.add(address);
            jobTasks.add(jobTask);
        }

        batchSaveJobTasks(jobTasks);

        return jobTasks;
    }

}
