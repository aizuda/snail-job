package com.aizuda.snailjob.server.job.task.support.generator.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskStatusEnum;
import com.aizuda.snailjob.common.core.enums.JobTaskTypeEnum;
import com.aizuda.snailjob.server.common.cache.CacheRegisterTable;
import com.aizuda.snailjob.server.common.dto.RegisterNodeInfo;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.util.ClientInfoUtils;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author opensnail
 * @date 2023-10-02 21:25:08
 * @since 2.4.0
 */
@Component
@Slf4j
public class BroadcastTaskGenerator extends AbstractJobTaskGenerator {

    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public JobTaskTypeEnum getTaskInstanceType() {
        return JobTaskTypeEnum.BROADCAST;
    }

    @Override
    @Transactional
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName(), context.getNamespaceId());
        if (CollUtil.isEmpty(serverNodes)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        Set<String> clientInfoSet = new HashSet<>(serverNodes.size());
        List<JobTask> jobTasks = new ArrayList<>(serverNodes.size());
        for (RegisterNodeInfo serverNode : serverNodes) {
            // 若存在相同的IP信息则去重
            String address = serverNode.address();
            if (clientInfoSet.contains(address)) {
                continue;
            }

            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientInfo(ClientInfoUtils.generate(serverNode));
            jobTask.setArgsType(context.getArgsType());
            jobTask.setArgsStr(context.getArgsStr());
            jobTask.setTaskStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new SnailJobServerException("新增任务实例失败"));
            clientInfoSet.add(address);
            jobTasks.add(jobTask);
        }

        return jobTasks;
    }

}
