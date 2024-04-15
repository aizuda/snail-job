package com.aizuda.snail.job.server.job.task.support.schedule;

import com.aizuda.snail.job.common.log.EasyRetryLog;
import com.aizuda.snail.job.server.common.Lifecycle;
import com.aizuda.snail.job.server.common.config.SystemProperties;
import com.aizuda.snail.job.server.common.dto.PartitionTask;
import com.aizuda.snail.job.server.common.schedule.AbstractSchedule;
import com.aizuda.snail.job.server.common.util.PartitionTaskUtils;
import com.aizuda.snail.job.server.job.task.dto.JobPartitionTaskDTO;
import com.aizuda.snail.job.server.job.task.support.JobTaskConverter;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snail.job.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snail.job.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTask;
import com.aizuda.snail.job.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job清理日志 一小时运行一次
 *
 * @author: opensnail
 * @date : 2023-07-21 13:32
 * @since 2.1.0
 */
@Component
@Slf4j
public class JobClearLogSchedule extends AbstractSchedule implements Lifecycle {

    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    private JobTaskMapper jobTaskMapper;
    @Autowired
    private JobLogMessageMapper jobLogMessageMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    // last clean log time
    private static Long lastCleanLogTime = 0L;

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

            EasyRetryLog.LOCAL.debug("Job clear success total:[{}]", total);
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("job clear log error", e);
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
                new LambdaUpdateWrapper<JobTaskBatch>().ge(JobTaskBatch::getId, startId)
                        .le(JobTaskBatch::getCreateDt, endTime)).getRecords();
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
                List<Long> ids = partitionTasks.stream().map(i -> i.getId()).collect(Collectors.toList());
                if (ids == null || ids.size() == 0) {
                    return;
                }
                jobTaskBatchMapper.deleteBatchIds(ids);

                // Waiting for deletion JobTaskList
                List<JobTask> jobTaskList = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>().in(JobTask::getTaskBatchId, ids));
                if (jobTaskList == null || jobTaskList.size() == 0) {
                    return;
                }
                List<Long> jobTaskListIds = jobTaskList.stream().map(i -> i.getId()).collect(Collectors.toList());
                jobTaskMapper.deleteBatchIds(jobTaskListIds);

                // Waiting for deletion JobLogMessageList
                List<JobLogMessage> jobLogMessageList = jobLogMessageMapper.selectList(new LambdaQueryWrapper<JobLogMessage>().in(JobLogMessage::getTaskBatchId, ids));
                if (jobLogMessageList == null || jobLogMessageList.size() == 0) {
                    return;
                }
                List<Long> jobLogMessageListIds = jobLogMessageList.stream().map(i -> i.getId()).collect(Collectors.toList());
                jobTaskMapper.deleteBatchIds(jobLogMessageListIds);
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
