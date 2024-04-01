package com.aizuda.easy.retry.server.job.task.support.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.Partition;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.schedule.AbstractSchedule;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.job.task.dto.JobPartitionTaskDTO;
import com.aizuda.easy.retry.server.job.task.support.JobTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * jogLogMessage 日志合并归档
 *
 * @author zhengweilin
 * @version 3.2.0
 * @date 2024/03/15
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLogMergeSchedule extends AbstractSchedule implements Lifecycle {

    private final SystemProperties systemProperties;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobLogMessageMapper jobLogMessageMapper;
    private final TransactionTemplate transactionTemplate;

    // last merge log time
    private static Long lastMergeLogTime = 0L;

    @Override
    public String lockName() {
        return "jobLogMerge";
    }

    @Override
    public String lockAtMost() {
        return "PT1H";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }

    @Override
    protected void doExecute() {
        try {
            // 合并日志数据最少保留最近一天的日志数据
            if (System.currentTimeMillis() - lastMergeLogTime < 24 * 60 * 60 * 1000) {
                return;
            }
            // merge job log
            long total;
            LocalDateTime endTime = LocalDateTime.now().minusDays(systemProperties.getMergeLogDays());
            total = PartitionTaskUtils.process(startId -> jobTaskBatchList(startId, endTime),
                this::processJobLogPartitionTasks, 0);

            EasyRetryLog.LOCAL.debug("job merge success total:[{}]", total);
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("job merge log error", e);
        } finally {
            // update merge time
            lastMergeLogTime = System.currentTimeMillis();
        }
    }

    /**
     * JobLog List
     *
     * @param startId
     * @param endTime
     * @return
     */
    private List<JobPartitionTaskDTO> jobTaskBatchList(Long startId, LocalDateTime endTime) {

        List<JobTaskBatch> jobTaskBatchList = jobTaskBatchMapper.selectPage(
            new Page<>(0, 1000),
            new LambdaUpdateWrapper<JobTaskBatch>().ge(JobTaskBatch::getId, startId)
                .in(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.COMPLETED)
                .le(JobTaskBatch::getCreateDt, endTime)).getRecords();
        return JobTaskConverter.INSTANCE.toJobTaskBatchPartitionTasks(jobTaskBatchList);
    }

    /**
     * merge job_log_message
     *
     * @param partitionTasks
     */
    public void processJobLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        // Waiting for merge JobTaskBatchList
        List<Long> ids = partitionTasks.stream().map(PartitionTask::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        // Waiting for deletion JobLogMessageList
        List<JobLogMessage> jobLogMessageList = jobLogMessageMapper.selectList(
            new LambdaQueryWrapper<JobLogMessage>().in(JobLogMessage::getTaskBatchId, ids));
        if (CollectionUtils.isEmpty(jobLogMessageList)) {
            return;
        }

        List<Map.Entry<Long, List<JobLogMessage>>> jobLogMessageGroupList = jobLogMessageList.stream().collect(
                groupingBy(JobLogMessage::getTaskId)).entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2).collect(toList());

        for (Map.Entry<Long/*taskId*/, List<JobLogMessage>> jobLogMessageMap : jobLogMessageGroupList) {
            List<Long> jobLogMessageDeleteBatchIds = new ArrayList<>();
            List<JobLogMessage> jobLogMessageInsertBatchIds = new ArrayList<>();

            List<String> mergeMessages = jobLogMessageMap.getValue().stream().map(k -> {
                    jobLogMessageDeleteBatchIds.add(k.getId());
                    return JsonUtil.parseObject(k.getMessage(), List.class);
                })
                .reduce((a, b) -> {
                    // 合并日志信息
                    List<String> list = new ArrayList<>();
                    list.addAll(a);
                    list.addAll(b);
                    return list;
                }).get();

            // 500条数据为一个批次
            List<List<String>> partitionMessages = Lists.partition(mergeMessages, systemProperties.getMergeLogNum());

            for (List<String> partitionMessage : partitionMessages) {
                // 深拷贝
                JobLogMessage jobLogMessage = JobTaskConverter.INSTANCE.toJobLogMessage(
                    jobLogMessageMap.getValue().get(0));

                jobLogMessage.setLogNum(partitionMessage.size());
                jobLogMessage.setMessage(JsonUtil.toJsonString(partitionMessage));
                jobLogMessageInsertBatchIds.add(jobLogMessage);
            }

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(final TransactionStatus status) {

                    // 批量删除、更新日志
                    if (CollectionUtil.isNotEmpty(jobLogMessageDeleteBatchIds)) {
                        jobLogMessageMapper.deleteBatchIds(jobLogMessageDeleteBatchIds);
                    }
                    if (CollectionUtil.isNotEmpty(jobLogMessageInsertBatchIds)) {
                        jobLogMessageMapper.batchInsert(jobLogMessageInsertBatchIds);
                    }
                }
            });
        }

    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1H"));
    }

    @Override
    public void close() {

    }
}
