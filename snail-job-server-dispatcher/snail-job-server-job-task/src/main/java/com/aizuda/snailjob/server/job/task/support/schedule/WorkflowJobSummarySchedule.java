package com.aizuda.snailjob.server.job.task.support.schedule;

import cn.hutool.core.collection.CollUtil;
import com.aizuda.snailjob.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.common.log.SnailJobLog;
import com.aizuda.snailjob.server.common.Lifecycle;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.JobTaskBatchReason;
import com.aizuda.snailjob.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.snailjob.server.common.schedule.AbstractSchedule;
import com.aizuda.snailjob.server.common.triple.Pair;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchSummaryResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobSummary;
import com.aizuda.snailjob.template.datasource.persistence.po.WorkflowTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Job Dashboard
 *
 * @author: wodeyangzipingpingwuqi
 * @date : 2024-03-25 10:16
 * @since 3.2.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowJobSummarySchedule extends AbstractSchedule implements Lifecycle {
    private final JobTaskBatchMapper jobTaskBatchMapper;
    private final JobSummaryMapper jobSummaryMapper;
    private final SystemProperties systemProperties;

    @Override
    public String lockName() {
        return "workflowJobSummarySchedule";
    }

    @Override
    public String lockAtMost() {
        return "PT1M";
    }

    @Override
    public String lockAtLeast() {
        return "PT20S";
    }

    @Override
    protected void doExecute() {
        try {
            for (int i = 0; i < systemProperties.getSummaryDay(); i++) {

                // 定时按日实时查询统计数据（00:00:00 - 23:59:59）
                LocalDateTime todayFrom = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusDays(-i);
                LocalDateTime todayTo = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).plusDays(-i);
                LambdaQueryWrapper<WorkflowTaskBatch> wrapper = new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .between(WorkflowTaskBatch::getCreateDt, todayFrom, todayTo)
                        .groupBy(WorkflowTaskBatch::getNamespaceId, WorkflowTaskBatch::getGroupName,
                                WorkflowTaskBatch::getWorkflowId, WorkflowTaskBatch::getTaskBatchStatus,
                                WorkflowTaskBatch::getOperationReason);
                List<JobBatchSummaryResponseDO> summaryWorkflowResponseDOList = jobTaskBatchMapper.selectWorkflowTaskBatchSummaryList(
                        wrapper);
                if (CollUtil.isEmpty(summaryWorkflowResponseDOList)) {
                    continue;
                }

                // insertOrUpdate
                List<JobSummary> jobSummaryList = jobSummaryList(todayFrom, summaryWorkflowResponseDOList);

                List<JobSummary> jobSummaries = jobSummaryMapper.selectList(new LambdaQueryWrapper<JobSummary>()
                        .eq(JobSummary::getTriggerAt, todayFrom)
                        .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.WORKFLOW.getType())
                        .in(JobSummary::getBusinessId, StreamUtils.toSet(jobSummaryList, JobSummary::getBusinessId)));

                Map<Pair<Long, LocalDateTime>, JobSummary> summaryMap = StreamUtils.toIdentityMap(jobSummaries,
                        jobSummary -> Pair.of(jobSummary.getBusinessId(), jobSummary.getTriggerAt()));

                List<JobSummary> waitInserts = Lists.newArrayList();
                List<JobSummary> waitUpdates = Lists.newArrayList();
                for (final JobSummary jobSummary : jobSummaryList) {
                    if (Objects.isNull(
                            summaryMap.get(Pair.of(jobSummary.getBusinessId(), jobSummary.getTriggerAt())))) {
                        waitInserts.add(jobSummary);
                    } else {
                        waitUpdates.add(jobSummary);
                    }
                }

                int updateTotalJobSummary = 0;
                if (CollUtil.isNotEmpty(waitUpdates)) {
                    updateTotalJobSummary = jobSummaryMapper.updateBatch(waitUpdates);
                }

                int insertTotalJobSummary = 0;
                if (CollUtil.isNotEmpty(waitInserts)) {
                    insertTotalJobSummary = jobSummaryMapper.insertBatch(waitInserts);
                }

                SnailJobLog.LOCAL.debug(
                        "workflow job summary dashboard success todayFrom:[{}] todayTo:[{}] updateTotalJobSummary:[{}] insertTotalJobSummary:[{}]",
                        todayFrom, todayTo, updateTotalJobSummary, insertTotalJobSummary);
            }
        } catch (Exception e) {
            SnailJobLog.LOCAL.error("workflow job summary dashboard log error", e);
        }
    }

    private List<JobSummary> jobSummaryList(LocalDateTime triggerAt,
                                            List<JobBatchSummaryResponseDO> summaryResponseDOList) {
        List<JobSummary> jobSummaryList = new ArrayList<>();
        Map<Long, List<JobBatchSummaryResponseDO>> jobIdListMap = summaryResponseDOList.parallelStream()
                .collect(Collectors.groupingBy(JobBatchSummaryResponseDO::getJobId));
        for (Map.Entry<Long, List<JobBatchSummaryResponseDO>> job : jobIdListMap.entrySet()) {
            JobSummary jobSummary = new JobSummary();
            jobSummary.setBusinessId(job.getKey());
            jobSummary.setTriggerAt(triggerAt);
            jobSummary.setNamespaceId(job.getValue().get(0).getNamespaceId());
            jobSummary.setGroupName(job.getValue().get(0).getGroupName());
            jobSummary.setSystemTaskType(SyetemTaskTypeEnum.WORKFLOW.getType());
            jobSummary.setSuccessNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getSuccessNum).sum());
            jobSummary.setFailNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getFailNum).sum());
            jobSummary.setStopNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getStopNum).sum());
            jobSummary.setCancelNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getCancelNum).sum());

            jobSummary.setFailReason(
                    JsonUtil.toJsonString(jobTaskBatchReasonList(JobTaskBatchStatusEnum.FAIL.getStatus(), job.getValue())));
            jobSummary.setStopReason(
                    JsonUtil.toJsonString(jobTaskBatchReasonList(JobTaskBatchStatusEnum.STOP.getStatus(), job.getValue())));
            jobSummary.setCancelReason(JsonUtil.toJsonString(
                    jobTaskBatchReasonList(JobTaskBatchStatusEnum.CANCEL.getStatus(), job.getValue())));
            jobSummaryList.add(jobSummary);
        }
        return jobSummaryList;
    }

    /**
     * 批次状态查询 (操作原因 && 总数)
     *
     * @param jobTaskBatchStatus
     * @param jobBatchSummaryResponseDOList
     * @return
     */
    private List<JobTaskBatchReason> jobTaskBatchReasonList(int jobTaskBatchStatus,
                                                            List<JobBatchSummaryResponseDO> jobBatchSummaryResponseDOList) {
        List<JobTaskBatchReason> jobTaskBatchReasonArrayList = new ArrayList<>();
        List<JobBatchSummaryResponseDO> summaryResponseDOList = jobBatchSummaryResponseDOList.stream()
                .filter(i -> jobTaskBatchStatus == i.getTaskBatchStatus()).collect(Collectors.toList());
        for (JobBatchSummaryResponseDO jobBatchSummaryResponseDO : summaryResponseDOList) {
            JobTaskBatchReason jobTaskBatchReason = new JobTaskBatchReason();
            jobTaskBatchReason.setReason(jobBatchSummaryResponseDO.getOperationReason());
            jobTaskBatchReason.setTotal(jobBatchSummaryResponseDO.getOperationReasonTotal());
            jobTaskBatchReasonArrayList.add(jobTaskBatchReason);
        }
        return jobTaskBatchReasonArrayList;
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1M"));
    }

    @Override
    public void close() {

    }
}
