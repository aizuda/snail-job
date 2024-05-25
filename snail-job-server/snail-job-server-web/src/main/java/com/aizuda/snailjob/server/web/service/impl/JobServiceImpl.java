package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.constant.SystemConstants;
import com.aizuda.snailjob.common.core.enums.StatusEnum;
import com.aizuda.snailjob.server.common.WaitStrategy;
import com.aizuda.snailjob.server.common.config.SystemProperties;
import com.aizuda.snailjob.server.common.enums.JobTaskExecutorSceneEnum;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.CronUtils;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.job.task.dto.JobTaskPrepareDTO;
import com.aizuda.snailjob.server.job.task.support.JobPrepareHandler;
import com.aizuda.snailjob.server.job.task.support.JobTaskConverter;
import com.aizuda.snailjob.server.job.task.support.cache.ResidentTaskCache;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobQueryVO;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import com.aizuda.snailjob.server.web.service.JobService;
import com.aizuda.snailjob.server.web.service.convert.JobConverter;
import com.aizuda.snailjob.server.web.service.convert.JobResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.access.AccessTemplate;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.GroupConfig;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author opensnail
 * @date 2023-10-11 22:20:42
 * @since 2.4.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final SystemProperties systemProperties;
    private final JobMapper jobMapper;
    @Lazy
    private final JobPrepareHandler terminalJobPrepareHandler;
    private final AccessTemplate accessTemplate;

    private static Long calculateNextTriggerAt(final JobRequestVO jobRequestVO, Long time) {
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return 0L;
        }

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(jobRequestVO.getTriggerType());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(jobRequestVO.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    @Override
    public PageResult<List<JobResponseVO>> getJobPage(JobQueryVO queryVO) {

        PageDTO<Job> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        PageDTO<Job> selectPage = jobMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .eq(Job::getNamespaceId, userSessionVO.getNamespaceId())
                        .in(userSessionVO.isUser(), Job::getGroupName, userSessionVO.getGroupNames())
                        .eq(StrUtil.isNotBlank(queryVO.getGroupName()), Job::getGroupName, queryVO.getGroupName())
                        .likeRight(StrUtil.isNotBlank(queryVO.getJobName()), Job::getJobName, StrUtil.trim(queryVO.getJobName()))
                        .eq(Objects.nonNull(queryVO.getJobStatus()), Job::getJobStatus, queryVO.getJobStatus())
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .orderByDesc(Job::getId));

        List<JobResponseVO> jobResponseList = JobResponseVOConverter.INSTANCE.convertList(selectPage.getRecords());

        return new PageResult<>(pageDTO, jobResponseList);
    }

    @Override
    public JobResponseVO getJobDetail(Long id) {
        Job job = jobMapper.selectById(id);
        return JobResponseVOConverter.INSTANCE.convert(job);
    }

    @Override
    public List<String> getTimeByCron(String cron) {
        return CronUtils.getExecuteTimeByCron(cron, 5);
    }

    @Override
    public List<JobResponseVO> getJobNameList(String keywords, Long jobId, String groupName) {

        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        PageDTO<Job> selectPage = jobMapper.selectPage(
                new PageDTO<>(1, 20),
                new LambdaQueryWrapper<Job>()
                        .select(Job::getId, Job::getJobName)
                        .eq(Job::getNamespaceId, userSessionVO.getNamespaceId())
                        .likeRight(StrUtil.isNotBlank(keywords), Job::getJobName, StrUtil.trim(keywords))
                        .eq(StrUtil.isNotBlank(groupName), Job::getGroupName, groupName)
                        .eq(Objects.nonNull(jobId), Job::getId, jobId)
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        // SQLServer 分页必须 ORDER BY
                        .orderByAsc(Job::getId));
        return JobResponseVOConverter.INSTANCE.convertList(selectPage.getRecords());
    }

    @Override
    public boolean saveJob(JobRequestVO jobRequestVO) {
        // 判断常驻任务
        Job job = updateJobResident(jobRequestVO);
        job.setBucketIndex(HashUtil.bkdrHash(jobRequestVO.getGroupName() + jobRequestVO.getJobName())
                % systemProperties.getBucketTotal());
        job.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, DateUtils.toNowMilli()));
        job.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        return 1 == jobMapper.insert(job);
    }

    @Override
    public boolean updateJob(JobRequestVO jobRequestVO) {
        Assert.notNull(jobRequestVO.getId(), () -> new SnailJobServerException("id 不能为空"));

        Job job = jobMapper.selectById(jobRequestVO.getId());
        Assert.notNull(job, () -> new SnailJobServerException("更新失败"));

        // 判断常驻任务
        Job updateJob = updateJobResident(jobRequestVO);
        updateJob.setNamespaceId(job.getNamespaceId());

        // 工作流任务
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            job.setNextTriggerAt(0L);
            // 非常驻任务 > 非常驻任务
        } else if (Objects.equals(job.getResident(), StatusEnum.NO.getStatus()) && Objects.equals(
                updateJob.getResident(),
                StatusEnum.NO.getStatus())) {
            updateJob.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, DateUtils.toNowMilli()));
        } else if (Objects.equals(job.getResident(), StatusEnum.YES.getStatus()) && Objects.equals(
                updateJob.getResident(), StatusEnum.NO.getStatus())) {
            // 常驻任务的触发时间
            long time = Optional.ofNullable(ResidentTaskCache.get(jobRequestVO.getId()))
                    .orElse(DateUtils.toNowMilli());
            updateJob.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, time));
            // 老的是不是常驻任务 新的是常驻任务 需要使用当前时间计算下次触发时间
        } else if (Objects.equals(job.getResident(), StatusEnum.NO.getStatus()) && Objects.equals(
                updateJob.getResident(), StatusEnum.YES.getStatus())) {
            updateJob.setNextTriggerAt(DateUtils.toNowMilli());
        }

        return 1 == jobMapper.updateById(updateJob);
    }

    @Override
    public Job updateJobResident(JobRequestVO jobRequestVO) {
        Job job = JobConverter.INSTANCE.convert(jobRequestVO);
        job.setResident(StatusEnum.NO.getStatus());
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return job;
        }

        if (jobRequestVO.getTriggerType() == WaitStrategies.WaitStrategyEnum.FIXED.getType()) {
            if (Integer.parseInt(jobRequestVO.getTriggerInterval()) < 10) {
                job.setResident(StatusEnum.YES.getStatus());
            }
        } else if (jobRequestVO.getTriggerType() == WaitStrategies.WaitStrategyEnum.CRON.getType()) {
            if (CronUtils.getExecuteInterval(jobRequestVO.getTriggerInterval()) < 10 * 1000) {
                job.setResident(StatusEnum.YES.getStatus());
            }
        } else {
            throw new SnailJobServerException("未知触发类型");
        }

        return job;
    }

    @Override
    public Boolean updateJobStatus(JobUpdateJobStatusRequestVO jobRequestVO) {
        Assert.notNull(jobRequestVO.getId(), () -> new SnailJobServerException("id 不能为空"));
        Assert.isTrue(1 == jobMapper.selectCount(new LambdaQueryWrapper<Job>().eq(Job::getId, jobRequestVO.getId())));

        Job job = new Job();
        job.setId(jobRequestVO.getId());
        job.setJobStatus(jobRequestVO.getJobStatus());
        return 1 == jobMapper.updateById(job);
    }

    @Override
    public Boolean deleteJobById(Long id) {
        Job job = new Job();
        job.setId(id);
        job.setDeleted(StatusEnum.YES.getStatus());
        return 1 == jobMapper.updateById(job);
    }

    @Override
    public boolean trigger(Long jobId) {

        Job job = jobMapper.selectById(jobId);
        Assert.notNull(job, () -> new SnailJobServerException("job can not be null."));

        long count = accessTemplate.getGroupConfigAccess().count(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, job.getGroupName())
                .eq(GroupConfig::getNamespaceId, job.getNamespaceId())
                .eq(GroupConfig::getGroupStatus, StatusEnum.YES.getStatus())
        );

        Assert.isTrue(count > 0, () -> new SnailJobServerException("组:[{}]已经关闭，不支持手动执行.", job.getGroupName()));
        JobTaskPrepareDTO jobTaskPrepare = JobTaskConverter.INSTANCE.toJobTaskPrepare(job);
        // 设置now表示立即执行
        jobTaskPrepare.setNextTriggerAt(DateUtils.toNowMilli());
        jobTaskPrepare.setTaskExecutorScene(JobTaskExecutorSceneEnum.MANUAL_JOB.getType());
        // 创建批次
        terminalJobPrepareHandler.handle(jobTaskPrepare);

        return Boolean.TRUE;
    }

    @Override
    public List<JobResponseVO> getJobList(String groupName) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        List<Job> jobs = jobMapper.selectList(
                new LambdaQueryWrapper<Job>()
                        .select(Job::getId, Job::getJobName)
                        .eq(Job::getNamespaceId, namespaceId)
                        .eq(Job::getGroupName, groupName)
                        .eq(Job::getDeleted, StatusEnum.NO.getStatus())
                        .orderByDesc(Job::getCreateDt));
        List<JobResponseVO> jobResponseList = JobResponseVOConverter.INSTANCE.convertList(jobs);
        return jobResponseList;
    }
}
