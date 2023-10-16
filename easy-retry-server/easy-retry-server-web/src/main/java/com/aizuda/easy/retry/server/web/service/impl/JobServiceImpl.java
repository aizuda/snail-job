package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.job.task.support.WaitStrategy;
import com.aizuda.easy.retry.server.job.task.support.strategy.WaitStrategies.WaitStrategyContext;
import com.aizuda.easy.retry.server.job.task.support.strategy.WaitStrategies.WaitStrategyEnum;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobQueryVO;
import com.aizuda.easy.retry.server.web.model.request.JobRequestVO;
import com.aizuda.easy.retry.server.web.model.request.JobUpdateJobStatusRequestVO;
import com.aizuda.easy.retry.server.web.model.response.JobResponseVO;
import com.aizuda.easy.retry.server.web.service.JobService;
import com.aizuda.easy.retry.server.web.service.convert.JobConverter;
import com.aizuda.easy.retry.server.web.service.convert.JobResponseVOConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author www.byteblogs.com
 * @date 2023-10-11 22:20:42
 * @since 2.4.0
 */
@Service
public class JobServiceImpl implements JobService {

    @Autowired
    private JobMapper jobMapper;

    @Override
    public PageResult<List<JobResponseVO>> getJobPage(JobQueryVO queryVO) {

        PageDTO<Job> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<Job> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Job::getDeleted, StatusEnum.NO.getStatus());
        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            queryWrapper.eq(Job::getGroupName, queryVO.getGroupName());
        }

        if (StrUtil.isNotBlank(queryVO.getJobName())) {
            queryWrapper.like(Job::getJobName, "%" + queryVO.getJobName() + "%");
        }

        if (Objects.nonNull(queryVO.getJobStatus())) {
            queryWrapper.eq(Job::getJobStatus, queryVO.getJobStatus());
        }

        queryWrapper.orderByDesc(Job::getId);
        PageDTO<Job> selectPage = jobMapper.selectPage(pageDTO, queryWrapper);

        List<JobResponseVO> jobResponseList = JobResponseVOConverter.INSTANCE.toJobResponseVOs(selectPage.getRecords());

        return new PageResult<>(pageDTO, jobResponseList);
    }

    @Override
    public JobResponseVO getJobDetail(Long id) {

        Job job = jobMapper.selectById(id);
        return JobResponseVOConverter.INSTANCE.toJobResponseVO(job);
    }

    @Override
    public boolean saveJob(JobRequestVO jobRequestVO) {
        Job job = JobConverter.INSTANCE.toJob(jobRequestVO);
        WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(jobRequestVO.getTriggerType());
        WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
        waitStrategyContext.setTriggerType(jobRequestVO.getTriggerType());
        waitStrategyContext.setTriggerInterval(jobRequestVO.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(LocalDateTime.now());
        job.setNextTriggerAt(waitStrategy.computeRetryTime(waitStrategyContext));
        return 1 == jobMapper.insert(job);
    }

    @Override
    public boolean updateJob(JobRequestVO jobRequestVO) {
        Assert.notNull(jobRequestVO.getId(), () -> new EasyRetryServerException("id 不能为空"));
        Assert.isTrue(1 == jobMapper.selectCount(new LambdaQueryWrapper<Job>().eq(Job::getId, jobRequestVO.getId())));
        Job job = JobConverter.INSTANCE.toJob(jobRequestVO);
        return 1 == jobMapper.updateById(job);
    }

    @Override
    public Boolean updateJobStatus(JobUpdateJobStatusRequestVO jobRequestVO) {
        Assert.notNull(jobRequestVO.getId(), () -> new EasyRetryServerException("id 不能为空"));
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
}
