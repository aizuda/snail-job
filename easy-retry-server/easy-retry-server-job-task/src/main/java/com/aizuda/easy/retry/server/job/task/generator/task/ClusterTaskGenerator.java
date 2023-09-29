package com.aizuda.easy.retry.server.job.task.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 12:59:53
 * @since 2.4.0
 */
@Component
@Slf4j
public class ClusterTaskGenerator extends AbstractJobTaskGenerator {

    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobMapper jobMapper;

    @Override
    public TaskTypeEnum getTaskInstanceType() {
        return TaskTypeEnum.CLUSTER;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {
        // 生成可执行任务
        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(context.getGroupName());
        if (Objects.isNull(serverNode)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        Job job = jobMapper.selectById(context.getJobId());

        // 新增任务实例
        JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
        jobTask.setClientId(serverNode.getHostId());
        jobTask.setArgsType(job.getArgsType());
        jobTask.setArgsStr(job.getArgsStr());
        jobTask.setExtAttrs(job.getExtAttrs());
        jobTask.setExecuteStatus(JobTaskStatusEnum.RUNNING.getStatus());
        jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
        Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增任务实例失败"));

        return Lists.newArrayList(jobTask);
    }

}
