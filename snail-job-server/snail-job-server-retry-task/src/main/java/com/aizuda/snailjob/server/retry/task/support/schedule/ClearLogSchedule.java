package com.aizuda.snailjob.server.retry.task.support.schedule;

import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
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

/**
 * Retry清理日志 一小时运行一次
 *
 * @author: opensnail
 * @date : 2023-07-21 13:32
 * @since 2.1.0
 */
@Component
@Slf4j
public class ClearLogSchedule extends AbstractSchedule implements Lifecycle {

    // last clean log time
    private static Long lastCleanLogTime = 0L;
    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public String lockName() {
        return "clearLog";
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
            if (systemProperties.getLogStorage() <= 0 || System.currentTimeMillis() - lastCleanLogTime < 24 * 60 * 60 * 1000) {
                return;
            }
            // clean retry log
            LocalDateTime endTime = LocalDateTime.now().minusDays(systemProperties.getLogStorage());
            long total = PartitionTaskUtils.process(startId -> retryTaskBatchList(startId, endTime),
                    this::processRetryLogPartitionTasks, 0);

            SnailJobLog.LOCAL.debug("Retry clear success total:[{}]", total);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("clear log error", e);
        } finally {
            // update clean time
            lastCleanLogTime = System.currentTimeMillis();
        }
    }

    /**
     * RetryLog List
     *
     * @param startId
     * @param endTime
     * @return
     */
    private List<RetryPartitionTask> retryTaskBatchList(Long startId, LocalDateTime endTime) {

        List<RetryTaskLog> retryTaskLogList = retryTaskLogMapper.selectPage(
                        new Page<>(0, 1000),
                        new LambdaUpdateWrapper<RetryTaskLog>()
                                .ge(RetryTaskLog::getId, startId)
                                .le(RetryTaskLog::getCreateDt, endTime)
                                .orderByAsc(RetryTaskLog::getId))
                .getRecords();
        return RetryTaskConverter.INSTANCE.toRetryTaskLogPartitionTasks(retryTaskLogList);
    }

    /**
     * clean table RetryTaskLog & RetryTaskLogMessage
     *
     * @param partitionTasks
     */
    public void processRetryLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                List<String> uniqueIdIds = StreamUtils.toList(partitionTasks, PartitionTask::getUniqueId);
                if (uniqueIdIds == null || uniqueIdIds.isEmpty()) {
                    return;
                }
                // Waiting for deletion RetryLog
                List<RetryTaskLog> retryTaskLogList = retryTaskLogMapper.selectList(new LambdaQueryWrapper<RetryTaskLog>().in(RetryTaskLog::getUniqueId, uniqueIdIds));
                if (retryTaskLogList != null && !retryTaskLogList.isEmpty()) {
                    List<Long> retryTaskListIds = StreamUtils.toList(retryTaskLogList, RetryTaskLog::getId);
                    retryTaskLogMapper.deleteByIds(retryTaskListIds);
                }

                // Waiting for deletion RetryTaskLogMessage
                List<RetryTaskLogMessage> retryTaskLogMessageList = retryTaskLogMessageMapper.selectList(new LambdaQueryWrapper<RetryTaskLogMessage>().in(RetryTaskLogMessage::getUniqueId, uniqueIdIds));
                if (retryTaskLogMessageList != null && !retryTaskLogMessageList.isEmpty()) {
                    List<Long> retryTaskListIds = StreamUtils.toList(retryTaskLogMessageList, RetryTaskLogMessage::getId);
                    retryTaskLogMessageMapper.deleteByIds(retryTaskListIds);
                }
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
