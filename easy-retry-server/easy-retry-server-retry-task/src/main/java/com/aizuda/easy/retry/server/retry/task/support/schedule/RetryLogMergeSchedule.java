package com.aizuda.easy.retry.server.retry.task.support.schedule;

import akka.io.SelectionHandler.Retry;
import cn.hutool.core.collection.CollectionUtil;
import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.core.enums.RetryStatusEnum;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.PartitionTask;
import com.aizuda.easy.retry.server.common.schedule.AbstractSchedule;
import com.aizuda.easy.retry.server.common.triple.Triple;
import com.aizuda.easy.retry.server.common.util.PartitionTaskUtils;
import com.aizuda.easy.retry.server.retry.task.dto.RetryMergePartitionTaskDTO;
import com.aizuda.easy.retry.server.retry.task.support.RetryTaskLogConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.RetryTaskLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLog;
import com.aizuda.easy.retry.template.datasource.persistence.po.RetryTaskLogMessage;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

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
public class RetryLogMergeSchedule extends AbstractSchedule implements Lifecycle {

    private final SystemProperties systemProperties;
    private final RetryTaskLogMapper retryTaskLogMapper;
    private final RetryTaskLogMessageMapper retryTaskLogMessageMapper;
    private final TransactionTemplate transactionTemplate;

    // last merge log time
    private static Long lastMergeLogTime = 0L;

    @Override
    public String lockName() {
        return "retryLogMerge";
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
    private List<RetryMergePartitionTaskDTO> jobTaskBatchList(Long startId, LocalDateTime endTime) {

        List<RetryTaskLog> jobTaskBatchList = retryTaskLogMapper.selectPage(
            new Page<>(0, 1000),
            new LambdaUpdateWrapper<RetryTaskLog>()
                .ge(RetryTaskLog::getId, startId)
                .in(RetryTaskLog::getRetryStatus, Lists.newArrayList(
                    RetryStatusEnum.FINISH.getStatus(),
                    RetryStatusEnum.MAX_COUNT.getStatus()))
                .le(RetryTaskLog::getCreateDt, endTime)).getRecords();
        return RetryTaskLogConverter.INSTANCE.toRetryMergePartitionTaskDTOs(jobTaskBatchList);
    }

    /**
     * merge job_log_message
     *
     * @param partitionTasks
     */
    public void processJobLogPartitionTasks(List<? extends PartitionTask> partitionTasks) {

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {

                // Waiting for merge RetryTaskLog
                List<String> ids = partitionTasks.stream().map(PartitionTask::getUniqueId).collect(Collectors.toList());
                if (CollectionUtil.isEmpty(ids)) {
                    return;
                }

                // Waiting for deletion RetryTaskLogMessage
                List<RetryTaskLogMessage> retryLogMessageList = retryTaskLogMessageMapper.selectList(
                    new LambdaQueryWrapper<RetryTaskLogMessage>().in(RetryTaskLogMessage::getUniqueId, ids));
                if (CollectionUtil.isEmpty(retryLogMessageList)) {
                    return;
                }

                List<Map.Entry<Triple<String, String, String>, List<RetryTaskLogMessage>>> jobLogMessageGroupList = retryLogMessageList.stream()
                    .collect(
                        groupingBy(message -> Triple.of(message.getNamespaceId(), message.getGroupName(),
                            message.getUniqueId())))
                    .entrySet().stream()
                    .filter(entry -> entry.getValue().size() >= 2).collect(toList());

                for (Map.Entry<Triple<String, String, String>/*taskId*/, List<RetryTaskLogMessage>> jobLogMessageMap : jobLogMessageGroupList) {
                    List<Long> jobLogMessageDeleteBatchIds = new ArrayList<>();

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
                    List<List<String>> partitionMessages = Lists.partition(mergeMessages,
                        systemProperties.getMergeLogNum());

                    List<RetryTaskLogMessage> jobLogMessageUpdateList = new ArrayList<>();

                    for (int i = 0; i < partitionMessages.size(); i++) {
                        // 深拷贝
                        RetryTaskLogMessage jobLogMessage = RetryTaskLogConverter.INSTANCE.toRetryTaskLogMessage(
                            jobLogMessageMap.getValue().get(0));
                        List<String> messages = partitionMessages.get(i);

                        jobLogMessage.setLogNum(messages.size());
                        jobLogMessage.setMessage(JsonUtil.toJsonString(messages));
                        jobLogMessageUpdateList.add(jobLogMessage);
                    }

                    // 批量删除、更新日志
                    if (CollectionUtil.isNotEmpty(jobLogMessageDeleteBatchIds)) {
                        retryTaskLogMessageMapper.deleteBatchIds(jobLogMessageDeleteBatchIds);
                    }
                    if (CollectionUtil.isNotEmpty(jobLogMessageUpdateList)) {
                        retryTaskLogMessageMapper.batchInsert(jobLogMessageUpdateList);
                    }
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
