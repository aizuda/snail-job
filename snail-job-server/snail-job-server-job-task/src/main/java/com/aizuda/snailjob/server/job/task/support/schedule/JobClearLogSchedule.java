package com.aizuda.snailjob.server.job.task.support.schedule;

import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.job.task.dto.JobPartitionTaskDTO;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.WorkflowTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Job清理日志 一小时运行一次
 *
 * @author: opensnail
 * @date : 2023-07-21 13:32
 * @since 2.1.0
 */
@Component
@RequiredArgsConstructor
public class JobClearLogSchedule extends AbstractSchedule implements Lifecycle {

    private final SystemProperties systemProperties;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobTaskMapper jobTaskMapper;
    private final JobLogMessageMapper jobLogMessageMapper;
    private final WorkflowTaskBatchMapper workflowTaskBatchMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public String lockName() {
        return "jobClearLog";
    }

    @Override
    public String lockAtMost() {
        return "PT4H";
    }

    @Override
    public String lockAtLeast() {
        return "PT1M";
    }

    @Override
    protected void doExecute() {
        try {
            // 清除日志默认保存天数大于零、最少保留最近一天的日志数据
            if (systemProperties.getLogStorage() < 1) {
                SnailJobLog.LOCAL.error("job clear log storage error", systemProperties.getLogStorage());
                return;
            }
            // clean job log
            long total;
            LocalDateTime endTime = LocalDateTime.now().minusDays(systemProperties.getLogStorage());
            total = PartitionTaskUtils.process(startId -> jobTaskBatchList(startId, endTime),
                    this::processJobLogPartitionTasks, 0);

            SnailJobLog.LOCAL.debug("Job clear success total:[{}]", total);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("job clear log error", e);
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
                        new LambdaUpdateWrapper<JobTaskBatch>()
                                .ge(JobTaskBatch::getId, startId)
                                .le(JobTaskBatch::getCreateDt, endTime)
                                .orderByAsc(JobTaskBatch::getId))
                .getRecords();
        return JobTaskConverter.INSTANCE.toJobTaskBatchPartitionTasks(jobTaskBatchList);
    }

    /**
     * clean table JobTaskBatch & JobTask & JobLogMessage
     *
     * @param partitionTasks
     */
    public void processJobLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        // Waiting for deletion JobTaskBatchList
        List<Long> partitionTasksIds = StreamUtils.toList(partitionTasks, PartitionTask::getId);
        if (CollectionUtils.isEmpty(partitionTasksIds)) {
            return;
        }
        List<List<Long>> idsPartition = Lists.partition(partitionTasksIds, 500);

        Set<Long> jobTaskListIds = new HashSet<>();
        Set<Long> jobLogMessageListIds = new HashSet<>();
        Set<Long> workflowBatchIds = new HashSet<>();
        for (List<Long> ids : idsPartition) {

            // Waiting for deletion JobTaskList
            List<JobTask> jobTaskList = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                    .select(JobTask::getId)
                    .in(JobTask::getTaskBatchId, ids));
            if (!CollectionUtils.isEmpty(jobTaskList)) {
                Set<Long> jobTask = jobTaskList.stream().map(JobTask::getId).collect(Collectors.toSet());
                jobTaskListIds.addAll(jobTask);
            }
            // Waiting for deletion JobLogMessageList
            List<JobLogMessage> jobLogMessageList = jobLogMessageMapper.selectList(new LambdaQueryWrapper<JobLogMessage>()
                    .select(JobLogMessage::getId)
                    .in(JobLogMessage::getTaskBatchId, ids));
            if (!CollectionUtils.isEmpty(jobLogMessageList)) {
                Set<Long> jobLogMessage = jobLogMessageList.stream().map(JobLogMessage::getId).collect(Collectors.toSet());
                jobLogMessageListIds.addAll(jobLogMessage);
            }

            // 先找出对应的 workflowTaskBatchId
            List<JobTaskBatch> jobTaskBatchList = jobTaskBatchMapper.selectList(new LambdaQueryWrapper<JobTaskBatch>().
                    select(JobTaskBatch::getWorkflowTaskBatchId)
                    .in(JobTaskBatch::getId, ids));
            if (!CollectionUtils.isEmpty(jobTaskBatchList)) {
                Set<Long> workflowTaskBatchId = jobTaskBatchList.stream().map(JobTaskBatch::getWorkflowTaskBatchId).collect(Collectors.toSet());
                workflowBatchIds.addAll(workflowTaskBatchId);
            }
        }

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                idsPartition.forEach(jobTaskBatchMapper::deleteByIds);
                if (!CollectionUtils.isEmpty(jobTaskListIds)) {
                    Lists.partition(jobTaskListIds.stream().toList(), 500).forEach(jobTaskMapper::deleteByIds);
                }
                if (!CollectionUtils.isEmpty(jobLogMessageListIds)) {
                    Lists.partition(jobLogMessageListIds.stream().toList(), 500).forEach(jobLogMessageMapper::deleteByIds);
                }
                if (!CollectionUtils.isEmpty(workflowBatchIds)) {
                    Lists.partition(workflowBatchIds.stream().toList(), 500).forEach(workflowTaskBatchMapper::deleteByIds);
                }
            }
        });
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT4H"));
    }

    @Override
    public void close() {

    }
}
