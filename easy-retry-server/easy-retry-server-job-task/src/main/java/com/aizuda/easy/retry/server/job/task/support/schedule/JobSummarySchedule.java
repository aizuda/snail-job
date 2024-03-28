package com.aizuda.easy.retry.server.job.task.support.schedule;

import com.aizuda.easy.retry.common.core.enums.JobTaskBatchStatusEnum;
import com.aizuda.easy.retry.common.log.EasyRetryLog;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.Lifecycle;
import com.aizuda.easy.retry.server.common.config.SystemProperties;
import com.aizuda.easy.retry.server.common.dto.JobTaskBatchReason;
import com.aizuda.easy.retry.server.common.enums.SyetemTaskTypeEnum;
import com.aizuda.easy.retry.server.common.schedule.AbstractSchedule;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchSummaryResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobSummary;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Job Dashboard
 *
 * @author: wodeyangzipingpingwuqi
 * @date : 2023-11-21 11:15
 * @since 2.5.0
 */
@Component
@Slf4j
public class JobSummarySchedule extends AbstractSchedule implements Lifecycle {

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    private JobSummaryMapper jobSummaryMapper;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public String lockName() {
        return "jobSummaryDashboard";
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
                LambdaQueryWrapper<JobTaskBatch> wrapper = new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getSystemTaskType, SyetemTaskTypeEnum.JOB.getType())
                        .between(JobTaskBatch::getCreateDt, todayFrom, todayTo)
                        .groupBy(JobTaskBatch::getNamespaceId, JobTaskBatch::getGroupName,
                                JobTaskBatch::getJobId, JobTaskBatch::getTaskBatchStatus, JobTaskBatch::getOperationReason);
                List<JobBatchSummaryResponseDO> summaryResponseDOList = jobTaskBatchMapper.summaryJobBatchList(wrapper);
                if (summaryResponseDOList == null || summaryResponseDOList.size() < 1) {
                    continue;
                }

                // insertOrUpdate
                List<JobSummary> jobSummaryList = jobSummaryList(todayFrom, summaryResponseDOList);
                int totalJobSummary = jobSummaryMapper.insertOrUpdate(jobSummaryList);
                EasyRetryLog.LOCAL.debug("job summary dashboard success todayFrom:[{}] todayTo:[{}] total:[{}]", todayFrom, todayTo, totalJobSummary);
            }
        } catch (Exception e) {
            EasyRetryLog.LOCAL.error("job summary dashboard log error", e);
        }
    }

    private List<JobSummary> jobSummaryList(LocalDateTime triggerAt, List<JobBatchSummaryResponseDO> summaryResponseDOList) {
        List<JobSummary> jobSummaryList = new ArrayList<>();
        Map<Long, List<JobBatchSummaryResponseDO>> jobIdListMap = summaryResponseDOList.parallelStream().collect(Collectors.groupingBy(JobBatchSummaryResponseDO::getJobId));
        for (Map.Entry<Long, List<JobBatchSummaryResponseDO>> job : jobIdListMap.entrySet()) {
            JobSummary jobSummary = new JobSummary();
            jobSummary.setBusinessId(job.getKey());
            jobSummary.setTriggerAt(triggerAt);
            jobSummary.setNamespaceId(job.getValue().get(0).getNamespaceId());
            jobSummary.setGroupName(job.getValue().get(0).getGroupName());
            jobSummary.setSystemTaskType(SyetemTaskTypeEnum.JOB.getType());
            jobSummary.setSuccessNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getSuccessNum).sum());
            jobSummary.setFailNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getFailNum).sum());
            jobSummary.setStopNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getStopNum).sum());
            jobSummary.setCancelNum(job.getValue().stream().mapToInt(JobBatchSummaryResponseDO::getCancelNum).sum());

            jobSummary.setFailReason(JsonUtil.toJsonString(jobTaskBatchReasonList(JobTaskBatchStatusEnum.FAIL.getStatus(), job.getValue())));
            jobSummary.setStopReason(JsonUtil.toJsonString(jobTaskBatchReasonList(JobTaskBatchStatusEnum.STOP.getStatus(), job.getValue())));
            jobSummary.setCancelReason(JsonUtil.toJsonString(jobTaskBatchReasonList(JobTaskBatchStatusEnum.CANCEL.getStatus(), job.getValue())));
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
    private List<JobTaskBatchReason> jobTaskBatchReasonList(int jobTaskBatchStatus, List<JobBatchSummaryResponseDO> jobBatchSummaryResponseDOList) {
        List<JobTaskBatchReason> jobTaskBatchReasonArrayList = new ArrayList<>();
        List<JobBatchSummaryResponseDO> summaryResponseDOList = jobBatchSummaryResponseDOList.stream().filter(i -> jobTaskBatchStatus == i.getTaskBatchStatus()).collect(Collectors.toList());
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
