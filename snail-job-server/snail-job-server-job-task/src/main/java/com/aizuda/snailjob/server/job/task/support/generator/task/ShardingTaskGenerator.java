package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobArgsTypeEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.model.JobArgsHolder;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 分片参数格式
 * 0=参数1;1=参数2;
 *
 * @author opensnail
 * @date 2023-10-02 21:37:22
 * @since 2.4.0
 */
@Component
@RequiredArgsConstructor
public class ShardingTaskGenerator extends AbstractJobTaskGenerator {
    private static final String TASK_NAME ="SHARDING_TASK";
    private final JobTaskMapper jobTaskMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.SHARDING;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollUtil.isEmpty(serverNodes)) {
            SnailJobLog.LOCAL.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        String argsStr = context.getArgsStr();
        if (StrUtil.isBlank(argsStr)) {
            SnailJobLog.LOCAL.error("切片参数为空. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        List<String> argsStrs;
        try {
            argsStrs = JsonUtil.parseList(argsStr, String.class);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("切片参数解析失败. jobId:[{}]", context.getJobId(), e);
            return Lists.newArrayList();
        }

        List<RegisterNodeInfo> nodeInfoList = new ArrayList<>(serverNodes);
        List<JobTask> jobTasks = new ArrayList<>(argsStrs.size());
        for (int index = 0; index < argsStrs.size(); index++) {
            RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
            // 新增任务实例
            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
            JobArgsHolder jobArgsHolder = new JobArgsHolder();
            jobArgsHolder.setJobParams(argsStrs.get(index));
            jobTask.setArgsStr(JsonUtil.toJsonString(jobArgsHolder));
            jobTask.setArgsType(JobArgsTypeEnum.JSON.getArgsType());
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            jobTask.setParentId(0L);
            jobTask.setRetryCount(0);
            jobTask.setLeaf(StatusEnum.YES.getStatus());
            jobTask.setCreateDt(LocalDateTime.now());
            jobTask.setUpdateDt(LocalDateTime.now());
            jobTask.setTaskName(TASK_NAME);
            jobTasks.add(jobTask);
        }

        batchSaveJobTasks(jobTasks);
        return jobTasks;
    }

}
