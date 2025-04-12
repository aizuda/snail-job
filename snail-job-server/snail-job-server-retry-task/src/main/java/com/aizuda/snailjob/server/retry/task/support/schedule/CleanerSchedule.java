package com.aizuda.snailjob.server.retry.task.support.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.context.SnailSpringContext;
import com.aizuda.snailjob.common.core.enums.RetryStatusEnum;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.retry.task.dto.RetryPartitionTask;
import com.aizuda.snailjob.server.retry.task.service.RetryDeadLetterConverter;
import com.aizuda.snailjob.server.retry.task.support.RetryTaskConverter;
import com.aizuda.snailjob.server.retry.task.support.event.RetryTaskFailDeadLetterAlarmEvent;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.access.TaskAccess;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Retry;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryDeadLetter;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTask;
import com.aizuda.snailjob.template.datasource.persistence.po.RetryTaskLogMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Retry清理线程
 * 1. 删除日志信息
 * 2. 删除重试已完成的数据
 * 3. 删除回调任务数据
 * 4. 删除调度日志 sj_retry_task
 * 5. 迁移到达最大重试的数据
 *
 * @author: opensnail
 * @date : 2023-07-21 13:32
 * @since 2.1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CleanerSchedule extends AbstractSchedule implements Lifecycle {
    private final RetryMapper retryMapper;
    private final RetryTaskMapper retryTaskMapper;
    private final SystemProperties systemProperties;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    private final TransactionTemplate transactionTemplate;
    private final AccessTemplate accessTemplate;

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
            if (systemProperties.getLogStorage() < 1) {
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
                        new Page<>(0, 500, Boolean.FALSE),
                        new LambdaUpdateWrapper<Retry>()
                                .ge(Retry::getId, startId)
                                .le(Retry::getCreateDt, endTime)
                                .eq(Retry::getTaskType, SyetemTaskTypeEnum.RETRY.getType())
                                .ne(Retry::getDeleted, StatusEnum.NO.getStatus())
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

        List<Long> retryIds = StreamUtils.toList(partitionTasks, PartitionTask::getId);
        if (CollectionUtils.isEmpty(retryIds)) {
            return;
        }

        // 查询回调数据
        List<Retry> cbRetries = retryMapper.selectList(new LambdaQueryWrapper<Retry>()
                .select(Retry::getId).in(Retry::getParentId, retryIds));

        List<Long> totalWaitRetryIds = Lists.newArrayList(retryIds);
        List<Long> cbRetryIds = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(cbRetries)) {
            cbRetryIds = StreamUtils.toList(cbRetries, Retry::getId);
            totalWaitRetryIds.addAll(cbRetryIds);
        }

        List<RetryPartitionTask> retryPartitionTasks = (List<RetryPartitionTask>) partitionTasks;

        List<Long> finishRetryIds = retryPartitionTasks.stream().filter(retryPartitionTask ->
                        RetryStatusEnum.FINISH.getStatus().equals(retryPartitionTask.getRetryStatus()))
                .map(PartitionTask::getId).toList();

        // 删除重试任务
        List<RetryTask> retryTaskList = retryTaskMapper.selectList(new LambdaQueryWrapper<RetryTask>()
                .in(RetryTask::getRetryId, totalWaitRetryIds));

        // 删除重试日志信息
        List<RetryTaskLogMessage> retryTaskLogMessageList = retryTaskLogMessageMapper.selectList(
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .in(RetryTaskLogMessage::getRetryId, totalWaitRetryIds));

        List<Long> finalCbRetryIds = cbRetryIds;
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                // 删除回调数据
                retryMapper.deleteByIds(finalCbRetryIds);

                // 删除重试完成的数据
                retryMapper.deleteByIds(finishRetryIds);

                // 删除重试任务
                if (!CollectionUtils.isEmpty(retryTaskList)) {
                    List<Long> retryTaskIds = StreamUtils.toList(retryTaskList, RetryTask::getId);
                    Lists.partition(retryTaskIds, 500).forEach(retryTaskMapper::deleteByIds);
                }

                if (!CollectionUtils.isEmpty(retryTaskLogMessageList)) {
                    List<Long> retryTaskLogMessageIds = StreamUtils.toList(retryTaskLogMessageList, RetryTaskLogMessage::getId);
                    Lists.partition(retryTaskLogMessageIds, 500).forEach(retryTaskLogMessageMapper::deleteByIds);
                }

            }
        });

        // 重试最大次数迁移死信表
        List<RetryPartitionTask> maxCountRetries = retryPartitionTasks.stream()
                .filter(retryPartitionTask ->
                        RetryStatusEnum.MAX_COUNT.getStatus().equals(retryPartitionTask.getRetryStatus()))
                .toList();
        moveDeadLetters(maxCountRetries);
    }

    /**
     * 迁移死信队列数据
     *
     * @param retries 待迁移数据
     */
    private void moveDeadLetters(List<RetryPartitionTask> retries) {
        if (CollUtil.isEmpty(retries)) {
            return;
        }

        List<RetryDeadLetter> retryDeadLetters = RetryDeadLetterConverter.INSTANCE.toRetryDeadLetter(retries);
        LocalDateTime now = LocalDateTime.now();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetters) {
            retryDeadLetter.setCreateDt(now);
        }

        Assert.isTrue(retryDeadLetters.size() == accessTemplate
                        .getRetryDeadLetterAccess().insertBatch(retryDeadLetters),
                () -> new SnailJobServerException("Failed to insert into dead letter queue [{}]", JsonUtil.toJsonString(retryDeadLetters)));

        TaskAccess<Retry> retryTaskAccess = accessTemplate.getRetryAccess();
        Assert.isTrue(retries.size() == retryTaskAccess.delete(new LambdaQueryWrapper<Retry>()
                        .in(Retry::getId, StreamUtils.toList(retries, RetryPartitionTask::getId))),
                () -> new SnailJobServerException("Failed to delete retry data [{}]", JsonUtil.toJsonString(retries)));

        SnailSpringContext.getContext().publishEvent(new RetryTaskFailDeadLetterAlarmEvent(
                RetryTaskConverter.INSTANCE.toRetryTaskFailDeadLetterAlarmEventDTO(retryDeadLetters)
        ));
    }


    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT4H"));
    }

    @Override
    public void close() {

    }
}
