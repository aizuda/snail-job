package com.aizuda.easy.retry.server.job.task.generator.task;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.JobTaskStatusEnum;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.dto.RegisterNodeInfo;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.handler.ClientNodeAllocateHandler;
import com.aizuda.easy.retry.server.job.task.JobTaskConverter;
import com.aizuda.easy.retry.server.job.task.enums.TaskTypeEnum;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.google.common.base.Splitter;
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
 * @author www.byteblogs.com
 * @date 2023-10-02 21:37:22
 * @since 2.4.0
 */
@Component
@Slf4j
public class ShardingTaskGenerator extends AbstractJobTaskGenerator {

    @Autowired
    private JobMapper jobMapper;
    @Autowired
    protected ClientNodeAllocateHandler clientNodeAllocateHandler;
    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public TaskTypeEnum getTaskInstanceType() {
        return TaskTypeEnum.SHARDING;
    }

    @Override
    public List<JobTask> doGenerate(JobTaskGenerateContext context) {

        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(context.getGroupName());
        if (CollectionUtils.isEmpty(serverNodes)) {
            log.error("无可执行的客户端信息. jobId:[{}]", context.getJobId());
            return Lists.newArrayList();
        }

        Job job = jobMapper.selectById(context.getJobId());
        String argsStr = job.getArgsStr();
        Map<String, String> split = Splitter.on(";").omitEmptyStrings().withKeyValueSeparator('=').split(argsStr);

        List<RegisterNodeInfo> nodeInfoList = new ArrayList<>(serverNodes);
        List<JobTask> jobTasks = new ArrayList<>(split.size());
        split.forEach((key, value) -> {
            RegisterNodeInfo registerNodeInfo = nodeInfoList.get(Integer.parseInt(key) % serverNodes.size());
            // 新增任务实例
            JobTask jobTask = JobTaskConverter.INSTANCE.toJobTaskInstance(context);
            jobTask.setClientId(registerNodeInfo.getHostId());
            jobTask.setArgsType(job.getArgsType());
            jobTask.setArgsStr(job.getArgsStr());
            jobTask.setExtAttrs(job.getExtAttrs());
            jobTask.setExecuteStatus(JobTaskStatusEnum.RUNNING.getStatus());
            jobTask.setResultMessage(Optional.ofNullable(jobTask.getResultMessage()).orElse(StrUtil.EMPTY));
            Assert.isTrue(1 == jobTaskMapper.insert(jobTask), () -> new EasyRetryServerException("新增任务实例失败"));
        });

        return jobTasks;
    }

}
