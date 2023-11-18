package com.aizuda.easy.retry.server.retry.task.support.schedule;

import com.aizuda.easy.retry.common.core.log.LogUtils;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.schedule.AbstractSchedule;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.*;
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
 * Retry清理日志 一小时运行一次
 *
 * @author: www.byteblogs.com
 * @date : 2023-07-21 13:32
 * @since 2.1.0
 */
@Component
@Slf4j
public class ClearLogSchedule extends AbstractSchedule implements Lifecycle {

    @Autowired
    private RetryTaskLogMapper retryTaskLogMapper;
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    // last clean log time
    private static Long lastCleanLogTime = 0L;

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

            LogUtils.debug(log, "Retry clear success total:[{}]", total);
        } catch (Exception e) {
            LogUtils.error(log, "clear log error", e);
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
                new LambdaUpdateWrapper<RetryTaskLog>().ge(RetryTaskLog::getId, startId).le(RetryTaskLog::getCreateDt, endTime)).getRecords();
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

                List<String> uniqueIdIds = partitionTasks.stream().map(i -> i.getUniqueId()).collect(Collectors.toList());
                if (uniqueIdIds == null || uniqueIdIds.size() == 0) {
                    return;
                }
                // Waiting for deletion RetryLog
                List<RetryTaskLog> retryTaskLogList = retryTaskLogMapper.selectList(new LambdaQueryWrapper<RetryTaskLog>().in(RetryTaskLog::getUniqueId, uniqueIdIds));
                if (retryTaskLogList != null && retryTaskLogList.size() > 0) {
                    List<Long> retryTaskListIds = retryTaskLogList.stream().map(i -> i.getId()).collect(Collectors.toList());
                    retryTaskLogMapper.deleteBatchIds(retryTaskListIds);
                }

                // Waiting for deletion RetryTaskLogMessage
                List<RetryTaskLogMessage> retryTaskLogMessageList = retryTaskLogMessageMapper.selectList(new LambdaQueryWrapper<RetryTaskLogMessage>().in(RetryTaskLogMessage::getUniqueId, uniqueIdIds));
                if (retryTaskLogMessageList != null && retryTaskLogMessageList.size() > 0) {
                    List<Long> retryTaskListIds = retryTaskLogMessageList.stream().map(i -> i.getId()).collect(Collectors.toList());
                    retryTaskLogMessageMapper.deleteBatchIds(retryTaskListIds);
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
