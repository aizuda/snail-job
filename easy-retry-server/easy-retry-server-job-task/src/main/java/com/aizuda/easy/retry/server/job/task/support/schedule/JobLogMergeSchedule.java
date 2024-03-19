package com.aizuda.easy.retry.server.job.task.support.schedule;

import cn.hutool.core.collection.CollectionUtil;
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

/**
 * jogLogMessage 日志合并归档
 *
 * @author zhengweilin
 * @version 3.2.0
 * @date 2024/03/15
 */
@Slf4j
@Component
public class JobLogMergeSchedule extends AbstractSchedule implements Lifecycle {

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
                        .eq(JobTaskBatch::getTaskBatchStatus, JobTaskBatchStatusEnum.SUCCESS.getStatus())
                        .le(JobTaskBatch::getCreateDt, endTime)).getRecords();
        return JobTaskConverter.INSTANCE.toJobTaskBatchPartitionTasks(jobTaskBatchList);
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

                // Waiting for merge JobTaskBatchList
                List<Long> ids = partitionTasks.stream().map(i -> i.getId()).collect(Collectors.toList());
                if (ids == null || ids.size() == 0) {
                    return;
                }

                // Waiting for deletion JobTaskList
                List<JobTask> jobTaskList = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>().in(JobTask::getTaskBatchId, ids));
                if (jobTaskList == null || jobTaskList.size() == 0) {
                    return;
                }

                // Waiting for deletion JobLogMessageList
                List<JobLogMessage> jobLogMessageList = jobLogMessageMapper.selectList(new LambdaQueryWrapper<JobLogMessage>().in(JobLogMessage::getTaskBatchId, ids));
                if (jobLogMessageList == null || jobLogMessageList.size() == 0) {
                    return;
                }

                List<Map.Entry<Long, List<JobLogMessage>>> jobLogMessageGroupList = jobLogMessageList.stream().collect(Collectors.groupingBy(JobLogMessage::getTaskId)).entrySet().stream()
                        .filter(entry -> entry.getValue().size() >= 2).collect(Collectors.toList());

                List<Long> jobLogMessageDeleteBatchIds = new ArrayList<>();
                List<JobLogMessage> jobLogMessageUpdateList = new ArrayList<>();
                for (Map.Entry<Long, List<JobLogMessage>> jobLogMessage : jobLogMessageGroupList) {
                    Integer sumLogNum = 0, jobLogMessageListNum = 0;
                    List mergeJobLogMessageList = new ArrayList();
                    for (int i = 0; i < jobLogMessage.getValue().size(); i++) {

                        // 累加合并数
                        JobLogMessage logMessage = jobLogMessage.getValue().get(i);
                        sumLogNum = sumLogNum + logMessage.getLogNum();

                        // 最后一次合并小于默认日志数量
                        if (jobLogMessageListNum == 0 && i == jobLogMessage.getValue().size() - 1) {
                            break;
                        }
                        // 需要合并日志数大于日志默认合并的条数,返回
                        if (jobLogMessageListNum == 0 && sumLogNum > systemProperties.getMergeLogNum()) {
                            sumLogNum = 0;
                            continue;
                        }
                        // 合并更新日志
                        logMessage.setLogNum(sumLogNum);
                        mergeJobLogMessageList.addAll(JsonUtil.parseObject(logMessage.getMessage(), List.class));
                        logMessage.setMessage(JsonUtil.toJsonString(mergeJobLogMessageList));
                        if (jobLogMessageListNum > 0 && sumLogNum > systemProperties.getMergeLogNum()) {
                            jobLogMessageUpdateList.add(logMessage);
                            mergeJobLogMessageList.clear();
                            sumLogNum = 0;
                            jobLogMessageListNum = 0;
                        } else if (jobLogMessageListNum > 0 && i == jobLogMessage.getValue().size() - 1) {
                            jobLogMessageUpdateList.add(logMessage);
                            mergeJobLogMessageList.clear();
                        } else {
                            jobLogMessageDeleteBatchIds.add(logMessage.getId());
                            jobLogMessageListNum++;
                        }
                    }
                    // GC
                    mergeJobLogMessageList.clear();
                }
                // 批量删除、更新日志
                if (CollectionUtil.isNotEmpty(jobLogMessageDeleteBatchIds)) {
                    jobLogMessageMapper.deleteBatchIds(jobLogMessageDeleteBatchIds);
                }
                if (CollectionUtil.isNotEmpty(jobLogMessageUpdateList)) {
                    jobLogMessageMapper.batchUpdate(jobLogMessageUpdateList);
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
