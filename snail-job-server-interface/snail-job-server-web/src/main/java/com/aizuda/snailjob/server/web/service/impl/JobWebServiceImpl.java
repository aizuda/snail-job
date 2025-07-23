package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.dto.PartitionTask;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.PartitionTaskUtils;
import com.aizuda.snailjob.server.common.util.TriggerIntervalUtils;
import com.aizuda.snailjob.server.job.task.support.JobPrepareHandler;
import com.aizuda.snailjob.server.service.dto.JobRequestBaseDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseBaseDTO;
import com.aizuda.snailjob.server.service.service.impl.AbstractJobService;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.*;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import com.aizuda.snailjob.server.web.service.JobWebService;
import com.aizuda.snailjob.server.web.service.convert.JobConverter;
import com.aizuda.snailjob.server.web.service.convert.JobResponseVOConverter;
import com.aizuda.snailjob.server.web.service.handler.GroupHandler;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobSummaryMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.SystemUserMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.SystemUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * @author opensnail
 * @date 2023-10-11 22:20:42
 * @since 2.4.0
 */
@Service("jobWebService")
@Slf4j
@RequiredArgsConstructor
@Validated
public class JobWebServiceImpl extends AbstractJobService implements JobWebService {

    private final SystemProperties systemProperties;
    private final JobMapper jobMapper;
    private final JobPrepareHandler terminalJobPrepareHandler;
    private final AccessTemplate accessTemplate;
    private final GroupHandler groupHandler;
    private final JobSummaryMapper jobSummaryMapper;
    private final SystemUserMapper systemUserMapper;

    private static Long calculateNextTriggerAt(final Job job, Long time) {
        if (Objects.equals(job.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return 0L;
        }

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(job.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(job.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    @Override
    public PageResult<List<JobResponseVO>> getJobPage(JobQueryVO queryVO) {

        PageDTO<Job> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(queryVO.getGroupName());

        PageDTO<Job> selectPage = jobMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getNamespaceId, userSessionVO.getNamespaceId())
                        .in(CollUtil.isNotEmpty(groupNames), Job::getGroupName, groupNames)
                        .like(StrUtil.isNotBlank(queryVO.getJobName()), Job::getJobName, StrUtil.trim(queryVO.getJobName()))
                        .like(StrUtil.isNotBlank(queryVO.getExecutorInfo()), Job::getExecutorInfo, StrUtil.trim(queryVO.getExecutorInfo()))
                        .eq(Objects.nonNull(queryVO.getJobStatus()), Job::getJobStatus, queryVO.getJobStatus())
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .eq(Objects.nonNull(queryVO.getOwnerId()), Job::getOwnerId, queryVO.getOwnerId())
                        .orderByDesc(Job::getId));
        List<JobResponseVO> jobResponseList = JobResponseVOConverter.INSTANCE.convertList(selectPage.getRecords());

        for (JobResponseVO jobResponseVO : jobResponseList) {
            // 兼容Oracle OwnerId Null查询异常：java.sql.SQLException: 无效的列类型: 1111
            if (Objects.nonNull(jobResponseVO.getOwnerId()) && jobResponseVO.getOwnerId() > 0) {
                SystemUser systemUser = systemUserMapper.selectById(jobResponseVO.getOwnerId());
                jobResponseVO.setOwnerName(systemUser.getUsername());
            }
        }
        return new PageResult<>(pageDTO, jobResponseList);
    }

//    @Override
//    public JobResponseVO getJobDetail(Long id) {
//        Job job = jobMapper.selectById(id);
//        return JobResponseVOConverter.INSTANCE.convert(job);
//    }

    @Override
    public List<String> getTimeByCron(String cron) {
        return CronUtils.getExecuteTimeByCron(cron, 5);
    }

    @Override
    public List<JobResponseVO> getJobNameList(String keywords, Long jobId, String groupName) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        PageDTO<Job> selectPage = jobMapper.selectPage(
                new PageDTO<>(1, 100),
                new LambdaQueryWrapper<Job>()
                        .select(Job::getId, Job::getJobName)
                        .eq(Job::getNamespaceId, userSessionVO.getNamespaceId())
                        .like(StrUtil.isNotBlank(keywords), Job::getJobName, StrUtil.trim(keywords))
                        .eq(StrUtil.isNotBlank(groupName), Job::getGroupName, groupName)
                        .eq(Objects.nonNull(jobId), Job::getId, jobId)
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        // SQLServer 分页必须 ORDER BY
                        .orderByDesc(Job::getId));
        return JobResponseVOConverter.INSTANCE.convertList(selectPage.getRecords());
    }

//    @Override
//    public boolean saveJob(JobRequestVO jobRequestVO) {
//        // 判断常驻任务
//        Job job = JobConverter.INSTANCE.convert(jobRequestVO);
//        job.setResident(isResident(jobRequestVO));
//
//        // check triggerInterval
//        checkTriggerInterval(jobRequestVO);
//
//        job.setBucketIndex(HashUtil.bkdrHash(jobRequestVO.getGroupName() + jobRequestVO.getJobName())
//                % systemProperties.getBucketTotal());
//        job.setNextTriggerAt(calculateNextTriggerAt(job, DateUtils.toNowMilli()));
//        job.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
//        job.setOwnerId(Optional.ofNullable(jobRequestVO.getOwnerId()).orElse(0L));
//        job.setId(null);
//        return 1 == jobMapper.insert(job);
//    }

    private void checkTriggerInterval(JobRequestVO jobRequestVO) {
        TriggerIntervalUtils.checkTriggerInterval(jobRequestVO.getTriggerInterval(), jobRequestVO.getTriggerType());
    }

//    @Override
//    public boolean updateJob(JobRequestVO jobRequestVO) {
//        Assert.notNull(jobRequestVO.getId(), () -> new SnailJobServerException("id cannot be null"));
//
//        Job job = jobMapper.selectById(jobRequestVO.getId());
//        Assert.notNull(job, () -> new SnailJobServerException("Update failed"));
//
//        // 判断常驻任务
//        Job updateJob = JobConverter.INSTANCE.convert(jobRequestVO);
//        updateJob.setResident(isResident(jobRequestVO));
//        updateJob.setNamespaceId(job.getNamespaceId());
//
//        // check triggerInterval
//        checkTriggerInterval(jobRequestVO);
//
//        // 工作流任务
////        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
////            updateJob.setNextTriggerAt(0L);
////            // 非常驻任务 > 非常驻任务
////        } else if (Objects.equals(job.getResident(), StatusEnum.NO.getStatus()) && Objects.equals(
////                updateJob.getResident(),
////                StatusEnum.NO.getStatus())) {
////            updateJob.setNextTriggerAt(calculateNextTriggerAt(updateJob, DateUtils.toNowMilli()));
////        } else if (Objects.equals(job.getResident(), StatusEnum.YES.getStatus()) && Objects.equals(
////                updateJob.getResident(), StatusEnum.NO.getStatus())) {
////            // 常驻任务的触发时间
////            long time = Optional.ofNullable(ResidentTaskCache.get(jobRequestVO.getId()))
////                    .orElse(DateUtils.toNowMilli());
////            updateJob.setNextTriggerAt(calculateNextTriggerAt(updateJob, time));
////            // 老的是不是常驻任务 新的是常驻任务 需要使用当前时间计算下次触发时间
////        } else if (Objects.equals(job.getResident(), StatusEnum.NO.getStatus()) && Objects.equals(
////                updateJob.getResident(), StatusEnum.YES.getStatus())) {
////            updateJob.setNextTriggerAt(DateUtils.toNowMilli());
////        }
//
//        CalculateNextTriggerAtDTO nextTriggerAtDTO = CalculateNextTriggerAtDTO
//                .builder()
//                .triggerInterval(jobRequestVO.getTriggerInterval())
//                .triggerType(jobRequestVO.getTriggerType())
//                .newResident(updateJob.getResident())
//                .oldResident(job.getResident())
//                .id(job.getId())
//                .build();
//        updateJob.setNextTriggerAt(JobKit.calculateNextTriggerAt(nextTriggerAtDTO));
//        // 禁止更新组
//        updateJob.setGroupName(null);
//        updateJob.setOwnerId(Optional.ofNullable(jobRequestVO.getOwnerId()).orElse(0L));
//
//        LambdaUpdateWrapper<Job> updateWrapper = new LambdaUpdateWrapper<>();
//        updateWrapper.eq(Job::getId, jobRequestVO.getId());
//        return 1 == jobMapper.update(updateJob, updateWrapper);
//    }

//    private Integer isResident(JobRequestVO jobRequestVO) {
//        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
//            return StatusEnum.NO.getStatus();
//        }
//
//        if (Objects.equals(jobRequestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.FIXED.getType())) {
//            if (Integer.parseInt(jobRequestVO.getTriggerInterval()) < 10) {
//                return StatusEnum.YES.getStatus();
//            }
//        } else if (Objects.equals(jobRequestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.CRON.getType())) {
//            if (CronUtils.getExecuteInterval(jobRequestVO.getTriggerInterval()) < 10 * 1000) {
//                return StatusEnum.YES.getStatus();
//            }
//        } else if (Objects.equals(jobRequestVO.getTriggerType(), WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType())) {
//            return StatusEnum.NO.getStatus();
//        } else {
//            throw new SnailJobServerException("Unknown trigger type");
//        }
//
//        return StatusEnum.NO.getStatus();
//    }

//    @Override
//    public Boolean updateJobStatus(JobStatusUpdateRequestVO jobRequestVO) {
//        Assert.notNull(jobRequestVO.getId(), () -> new SnailJobServerException("id cannot be null"));
//        Job job = new Job();
//        if (Objects.equals(jobRequestVO.getJobStatus(), StatusEnum.YES.getStatus())) {
//            Job dbJob = jobMapper.selectById(jobRequestVO.getId());
//            Assert.notNull(job, () -> new SnailJobServerException("Update failed"));
//            job.setId(jobRequestVO.getId());
//            job.setJobStatus(jobRequestVO.getJobStatus());
//            // 如果是开启任务，需要重新计算下次触发时间
//            job.setNextTriggerAt(calculateNextTriggerAt(dbJob, DateUtils.toNowMilli()));
//        } else {
//            job.setId(jobRequestVO.getId());
//            job.setJobStatus(jobRequestVO.getJobStatus());
//        }
//        return 1 == jobMapper.updateById(job);
//    }

//    @Override
//    public boolean trigger(JobTriggerVO jobTrigger) {
//        Job job = jobMapper.selectById(jobTrigger.getJobId());
//        Assert.notNull(job, () -> new SnailJobServerException("job can not be null."));
//
//        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
//                .eq(GroupConfig::getGroupName, job.getGroupName())
//                .eq(GroupConfig::getNamespaceId, job.getNamespaceId())
//                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
//        );
//
//        Assert.isTrue(count > 0,
//                () -> new SnailJobServerException("Group [{}] is closed, manual execution is not supported.", job.getGroupName()));
//        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
//        // 设置now表示立即执行
//        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli());
//        jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_JOB.getType());
//        if (StrUtil.isNotBlank(jobTrigger.getTmpArgsStr())) {
//            jobTaskPrepare.setTmpArgsStr(jobTrigger.getTmpArgsStr());
//        }
//        // 创建批次
//        terminalJobPrepareHandler.handle(jobTaskPrepare);
//
//        return Boolean.TRUE;
//    }

    @Override
    public List<JobResponseVO> getJobList(String groupName) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        List<Job> jobs = jobMapper.selectList(
                new LambdaQueryWrapper<Job>()
                        .select(Job::getId, Job::getJobName, Job::getExecutorInfo, Job::getTaskType, Job::getLabels)
                        .eq(Job::getNamespaceId, namespaceId)
                        .eq(Job::getGroupName, groupName)
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .orderByDesc(Job::getCreateDt));
        return JobResponseVOConverter.INSTANCE.convertList(jobs);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importJobs(List<JobRequestVO> requestList) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        groupHandler.validateGroupExistence(
                StreamUtils.toSet(requestList, JobRequestVO::getGroupName), namespaceId
        );
        requestList.forEach(this::addJob);
    }

    @Override
    public String exportJobs(ExportJobVO exportJobVO) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        List<JobRequestVO> requestList = new ArrayList<>();
        PartitionTaskUtils.process(startId -> {
                    List<Job> jobList = jobMapper.selectPage(new PageDTO<>(0, 100, Boolean.FALSE),
                            new LambdaQueryWrapper<Job>()
                                    .eq(Job::getNamespaceId, namespaceId)
                                    .eq(StrUtil.isNotBlank(exportJobVO.getGroupName()), Job::getGroupName, exportJobVO.getGroupName())
                                    .likeRight(StrUtil.isNotBlank(exportJobVO.getJobName()), Job::getJobName, StrUtil.trim(exportJobVO.getJobName()))
                                    .eq(Objects.nonNull(exportJobVO.getJobStatus()), Job::getJobStatus, exportJobVO.getJobStatus())
                                    .in(CollUtil.isNotEmpty(exportJobVO.getJobIds()), Job::getId, exportJobVO.getJobIds())
                                    .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                                    .gt(Job::getId, startId)
                                    .orderByAsc(Job::getId)
                    ).getRecords();
                    return StreamUtils.toList(jobList, JobPartitionTask::new);
                },
                partitionTasks -> {
                    List<JobPartitionTask> jobPartitionTasks = (List<JobPartitionTask>) partitionTasks;
                    requestList.addAll(
                            JobConverter.INSTANCE.convertList(StreamUtils.toList(jobPartitionTasks, JobPartitionTask::getJob)));
                }, 0);

        return JsonUtil.toJsonString(requestList);
    }

//    @Override
//    @Transactional
//    public Boolean deleteJobByIds(Set<Long> ids) {
//        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
//
//        Assert.isTrue(ids.size() == jobMapper.delete(
//                new LambdaQueryWrapper<Job>()
//                        .eq(Job::getNamespaceId, namespaceId)
//                        .eq(Job::getJobStatus, StatusEnum.NO.getStatus())
//                        .in(Job::getId, ids)
//        ), () -> new SnailJobServerException("Failed to delete scheduled task, please check if the task status is closed"));
//
//        List<JobSummary> jobSummaries = jobSummaryMapper.selectList(new LambdaQueryWrapper<JobSummary>()
//                .select(JobSummary::getId)
//                .in(JobSummary::getBusinessId, ids)
//                .eq(JobSummary::getNamespaceId, namespaceId)
//                .eq(JobSummary::getSystemTaskType, SyetemTaskTypeEnum.JOB.getType())
//        );
//        if (CollUtil.isNotEmpty(jobSummaries)) {
//            jobSummaryMapper.deleteByIds(StreamUtils.toSet(jobSummaries, JobSummary::getId));
//        }
//
//        return Boolean.TRUE;
//    }

    @Override
    protected void getJobByIdAfter(JobResponseBaseDTO responseBaseDTO, Job job) {
        JobResponseVO  jobResponseVO = (JobResponseVO) responseBaseDTO;
        SystemUser systemUser = systemUserMapper.selectById(job.getOwnerId());
        if (Objects.nonNull(systemUser)) {
            jobResponseVO.setOwnerName(systemUser.getUsername());
        }
        jobResponseVO.setOwnerId(job.getOwnerId());
    }

    @Override
    protected void updateJobPreValidator(JobRequestBaseDTO jobRequest) {

    }

    @Override
    protected String getNamespaceId() {
        return UserSessionUtils.currentUserSession().getNamespaceId();
    }

    @Override
    protected void addJobPopulate(Job job, JobRequestBaseDTO request) {

    }

    @Override
    protected void addJobPreValidator(JobRequestBaseDTO request) {

    }

    @EqualsAndHashCode(callSuper = true)
    @Getter
    private static class JobPartitionTask extends PartitionTask {

        // 这里就直接放GroupConfig为了后面若加字段不需要再这里在调整了
        private final Job job;

        public JobPartitionTask(@NotNull Job job) {
            this.job = job;
            setId(job.getId());
        }
    }

}
