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
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
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

    @Autowired
    private RetryMapper retryMapper;
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
            if (systemProperties.getLogStorage() <= 1) {
                SnailJobLog.LOCAL.error("retry clear log storage error", systemProperties.getLogStorage());
                return;
            }
            // clean retry log
            LocalDateTime endTime = LocalDateTime.now().minusDays(systemProperties.getLogStorage());
            long total = PartitionTaskUtils.process(startId -> retryTaskBatchList(startId, endTime),
                    this::processRetryLogPartitionTasks, 0);

            SnailJobLog.LOCAL.debug("Retry clear success total:[{}]", total);
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("clear log error", e);
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

        List<Retry> retryTaskList = retryMapper.selectPage(
                        new Page<>(0, 1000),
                        new LambdaUpdateWrapper<Retry>()
                                .ge(Retry::getId, startId)
                                .le(Retry::getCreateDt, endTime)
                                .orderByAsc(Retry::getId))
                .getRecords();
        return RetryTaskConverter.INSTANCE.toRetryTaskLogPartitionTasks(retryTaskList);
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

                List<Long> uniqueIdIds = StreamUtils.toList(partitionTasks, PartitionTask::getId);
                if (uniqueIdIds == null || uniqueIdIds.isEmpty()) {
                    return;
                }
                // Waiting for deletion RetryLog
//                List<RetryTask> retryTaskList = retryMapper.selectList(new LambdaQueryWrapper<RetryTask>().in(RetryTask::getId, uniqueIdIds));
//                if (retryTaskList != null && !retryTaskList.isEmpty()) {
//                    List<Long> retryTaskListIds = StreamUtils.toList(retryTaskList, RetryTask::getId);
//                    retryTaskMapper.deleteByIds(retryTaskListIds);
//                }

                // Waiting for deletion RetryTaskLogMessage
                List<RetryTaskLogMessage> retryTaskLogMessageList = retryTaskLogMessageMapper.selectList(new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .in(RetryTaskLogMessage::getRetryId, uniqueIdIds));
                if (retryTaskLogMessageList != null && !retryTaskLogMessageList.isEmpty()) {
                    List<Long> retryTaskListIds = StreamUtils.toList(retryTaskLogMessageList, RetryTaskLogMessage::getId);
                    retryTaskLogMessageMapper.deleteByIds(retryTaskListIds);
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
