package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.InstanceLiveInfo;
import com.aizuda.snailjob.server.common.dto.InstanceSelectCondition;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snailjob.server.common.handler.InstanceManager;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author opensnail
 * @date 2023-10-02 12:59:53
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class ClusterTaskGenerator extends AbstractJobTaskGenerator {
    private static final String TASK_NAME ="CLUSTER_TASK";
    private final InstanceManager instanceManager;
    private final JobTaskMapper jobTaskMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.CLUSTER;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {
        // 生成可执行任务
        InstanceSelectCondition condition = InstanceSelectCondition
                .builder()
                .namespaceId(context.getNamespaceId())
                .groupName(context.getGroupName())
                .routeKey(context.getRouteKey())
                .allocKey(String.valueOf(context.getJobId()))
                .targetLabel(context.getLabels() != null ? JsonUtil.parseHashMap(context.getLabels()) : null)
                .build();
        InstanceLiveInfo routeKey = instanceManager.getALiveInstanceByRouteKey(condition);
        if (Objects.isNull(routeKey)) {
            SnailJobLog.LOCAL.error("No executable client information. Job ID:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        // 新增任务实例
        JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
        jobTask.setClientInfo(ClientInfoUtils.generate(routeKey.getNodeInfo()));
        jobTask.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        JobArgsHolder jobArgsHolder = new JobArgsHolder();
        jobArgsHolder.setJobParams(context.getArgsStr());
        jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
        jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
        jobTask.setTaskName(TASK_NAME);
        jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new SnailJobServerException("Adding new task instance failed"));

        return Lists.newArrayList(jobTask);
    }

}
