package com.aizuda.easy.retry.server.job.task.support.generator.batch;

import cn.hutool.core.lang.Assert;
import com.aizuda.easy.retry.common.core.enums.JobOperationReasonEnum;
import com.aizuda.easy.retry.server.common.cache.CacheRegisterTable;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.common.util.DateUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobTimerTaskDTO;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerTask;
import com.aizuda.easy.retry.server.job.task.support.timer.JobTimerWheel;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2023-10-02 10:22:26
 * @since 2.4.0
 */
@Component
@Slf4j
public class JobTaskBatchGenerator {

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;

    @Transactional
    public void generateJobTaskBatch(JobTaskBatchGeneratorContext context) {

        // 生成一个新的任务
        JobTaskBatch jobTaskBatch = new JobTaskBatch();
        jobTaskBatch.setJobId(context.getJobId());
        jobTaskBatch.setGroupName(context.getGroupName());
        jobTaskBatch.setCreateDt(LocalDateTime.now());
        // 无执行的节点
        if (CollectionUtils.isEmpty(CacheRegisterTable.getServerNodeSet(context.getGroupName()))) {
            jobTaskBatch.setTaskBatchStatus(JobTaskBatchStatusEnum.CANCEL.getStatus());
            jobTaskBatch.setOperationReason(JobOperationReasonEnum.NOT_CLIENT.getReason());
        } else {
            // 生成一个新的任务
            jobTaskBatch.setTaskBatchStatus(Optional.ofNullable(context.getTaskBatchStatus()).orElse(JobTaskBatchStatusEnum.WAITING.getStatus()));
            jobTaskBatch.setOperationReason(context.getOperationReason());
        }

        Assert.isTrue(1 == jobTaskBatchMapper.insert(jobTaskBatch), () -> new EasyRetryServerException("新增调度任务失败.jobId:[{}]", context.getJobId()));

        // 非待处理状态无需进入时间轮中
        if (JobTaskBatchStatusEnum.WAITING.getStatus() != jobTaskBatch.getTaskBatchStatus()) {
            return;
        }

        // 进入时间轮
        long delay = context.getNextTriggerAt() - DateUtils.toNowMilli();
        JobTimerTaskDTO jobTimerTaskDTO = new JobTimerTaskDTO();
        jobTimerTaskDTO.setTaskBatchId(jobTaskBatch.getId());
        jobTimerTaskDTO.setGroupName(context.getGroupName());
        jobTimerTaskDTO.setJobId(context.getJobId());
        JobTimerWheel.register(jobTaskBatch.getId(),
                new JobTimerTask(jobTimerTaskDTO), delay, TimeUnit.MILLISECONDS);

    }

}
