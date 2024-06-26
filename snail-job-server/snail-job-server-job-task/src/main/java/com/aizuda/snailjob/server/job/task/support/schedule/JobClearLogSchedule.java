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
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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

    // last clean log time
    private static Long lastCleanLogTime = 0L;
    private final SystemProperties systemProperties;
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobTaskMapper jobTaskMapper;
    private final JobLogMessageMapper jobLogMessageMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public String lockName() {
        return "jobClearLog";
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
            // 清楚日志默认保存天数大于零、最少保留最近一天的日志数据
            if (systemProperties.getLogStorage() <= 0 || System.currentTimeMillis() - lastCleanLogTime < 24 * 60 * 60 * 1000) {
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
        } finally {
            // update clean time
            lastCleanLogTime = System.currentTimeMillis();
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

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                // Waiting for deletion JobTaskBatchList
                List<Long> ids = StreamUtils.toList(partitionTasks, PartitionTask::getId);
                if (CollectionUtils.isEmpty(ids)) {
                    return;
                }
                Lists.partition(ids, 500).forEach(partIds -> jobTaskBatchMapper.deleteByIds(ids));

                // Waiting for deletion JobTaskList
                List<JobTask> jobTaskList = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>().in(JobTask::getTaskBatchId, ids));
                if (CollectionUtils.isEmpty(jobTaskList)) {
                    return;
                }
                List<Long> jobTaskListIds = StreamUtils.toList(jobTaskList, JobTask::getId);
                Lists.partition(jobTaskListIds, 500).forEach(partIds -> jobTaskMapper.deleteByIds(partIds));

                // Waiting for deletion JobLogMessageList
                List<JobLogMessage> jobLogMessageList = jobLogMessageMapper.selectList(new LambdaQueryWrapper<JobLogMessage>().in(JobLogMessage::getTaskBatchId, ids));
                if (CollectionUtils.isEmpty(jobLogMessageList)) {
                    return;
                }
                List<Long> jobLogMessageListIds = StreamUtils.toList(jobLogMessageList, JobLogMessage::getId);
                Lists.partition(jobLogMessageListIds, 500).forEach(partIds -> jobTaskMapper.deleteByIds(jobLogMessageListIds));
            }
        });
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1H"));
    }

    @Override
    public void close() {

    }
}
