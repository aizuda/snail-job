package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.handler.ClientNodeAllocateHandler;
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
    private final ClientNodeAllocateHandler clientNodeAllocateHandler;
    private final JobTaskMapper jobTaskMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.CLUSTER;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {
        // 生成可执行任务
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(context.getJobId().toString(),
                context.getGroupName(), context.getNamespaceId(), context.getRouteKey());
        if (Objects.isNull(serverNode)) {
            SnailJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        // 新增任务实例
        JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
        jobTask.setClientInfo(ClientInfoUtils.generate(serverNode));
        jobTask.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
        JobArgsHolder jobArgsHolder = new JobArgsHolder();
        jobArgsHolder.setJobParams(context.getArgsStr());
        jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
        jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
        jobTask.setTaskName(TASK_NAME);
        jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new SnailJobServerException("新增任务实例失败"));

        return Lists.newArrayList(jobTask);
    }

}
