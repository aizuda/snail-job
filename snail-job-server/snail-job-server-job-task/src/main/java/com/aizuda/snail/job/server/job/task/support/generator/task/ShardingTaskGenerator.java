package com.aizuda.snail.job.server.job.task.support.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snail.job.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snail.job.common.core.util.JsonUtil;
import com.aizuda.snail.job.server.common.cache.CacheRegisterTable;
import com.aizuda.snail.job.server.common.dto.RegisterNodeInfo;
import com.aizuda.snail.job.server.common.exception.EasyRetryServerException;
import com.aizuda.snail.job.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.snail.job.server.common.util.ClientInfoUtils;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import com.aizuda.snail.job.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTask;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 分片参数格式
 * 0=参数1;1=参数2;
 *
 * @author opensnail
 * @date 2023-10-02 21:37:22
 * @since 2.4.0
 */
@Component
@Slf4j
public class ShardingTaskGenerator extends AbstractJobTaskGenerator {

    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.SHARDING;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollectionUtils.isEmpty(serverNodes)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        String argsStr = context.getArgsStr();
        if (StrUtil.isBlank(argsStr)) {
            log.error("切片参数为空. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        List<String> argsStrs;
        try {
            argsStrs = JsonUtil.parseList(argsStr, String.class);
        } catch (Exception e) {
            log.error("切片参数解析失败. jobId:[{}]", context.getJobId(), e);
            return Lists.newArrayList();
        }

        List<RegisterNodeInfo> nodeInfoList = new ArrayList<>(serverNodes);
        List<JobTask> jobTasks = new ArrayList<>(argsStrs.size());
        for (int index = 0; index < argsStrs.size(); index++) {
            RegisterNodeInfo registerNodeInfo = nodeInfoList.get(index % serverNodes.size());
            // 新增任务实例
            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(registerNodeInfo));
            jobTask.setArgsType(context.getArgsType());
            jobTask.setArgsStr(argsStrs.get(index));
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增任务实例失败"));
            jobTasks.add(jobTask);
        }

        return jobTasks;
    }

}
