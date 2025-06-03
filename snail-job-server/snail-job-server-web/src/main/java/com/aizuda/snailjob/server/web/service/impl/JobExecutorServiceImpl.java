package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobExecutorQueryVO;
import com.aizuda.snailjob.server.web.model.request.UserSessionVO;
import com.aizuda.snailjob.server.web.service.JobExecutorService;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobExecutorsMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @Author：srzou
 * @Package：com.aizuda.snailjob.server.web.service.impl
 * @Project：snail-job
 * @Date：2025/6/3 13:27
 * @Filename：JobExecutorServiceImpl
 */
@Service
@RequiredArgsConstructor
public class JobExecutorServiceImpl implements JobExecutorService {
    private JobExecutorsMapper jobExecutorsMapper;

    @Override
    public PageResult<List<JobExecutors>> getJobExecutorPage(JobExecutorQueryVO jobQueryVO) {
        PageDTO<JobExecutors> pageDTO = new PageDTO<>(jobQueryVO.getPage(), jobQueryVO.getSize());
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(jobQueryVO.getGroupName());

        PageDTO<JobExecutors> selectPage = jobExecutorsMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<JobExecutors>()
                        .eq(JobExecutors::getNamespaceId, userSessionVO.getNamespaceId())
                        .eq(StrUtil.isNotBlank(jobQueryVO.getExecutorType()), JobExecutors::getExecutorType, jobQueryVO.getExecutorType())
                        .in(CollUtil.isNotEmpty(groupNames), JobExecutors::getGroupName, groupNames)
                        .like(StrUtil.isNotBlank(jobQueryVO.getExecutorInfo()), JobExecutors::getJobExecutorsName, StrUtil.trim(jobQueryVO.getExecutorInfo()))
                        .orderByDesc(JobExecutors::getId));

        return new PageResult<>(pageDTO, selectPage.getRecords());
    }

    @Override
    public JobExecutors getJobExecutorDetail(Long id) {
        return jobExecutorsMapper.selectById(id);
    }

    @Override
    public List<JobExecutors> getJobExecutorList(JobExecutorQueryVO jobQueryVO) {
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(jobQueryVO.getGroupName());

        return jobExecutorsMapper.selectList(
                new LambdaQueryWrapper<JobExecutors>()
                        .eq(JobExecutors::getNamespaceId, userSessionVO.getNamespaceId())
                        .eq(StrUtil.isNotBlank(jobQueryVO.getExecutorType()), JobExecutors::getExecutorType, jobQueryVO.getExecutorType())
                        .in(CollUtil.isNotEmpty(groupNames), JobExecutors::getGroupName, groupNames)
                        .like(StrUtil.isNotBlank(jobQueryVO.getExecutorInfo()), JobExecutors::getJobExecutorsName, StrUtil.trim(jobQueryVO.getExecutorInfo()))
                        .orderByDesc(JobExecutors::getId));
    }

    @Override
    @Transactional
    public Boolean deleteJobExecutorByIds(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        Assert.isTrue(ids.size() == jobExecutorsMapper.delete(
                new LambdaQueryWrapper<JobExecutors>()
                        .eq(JobExecutors::getNamespaceId, namespaceId)
                        .in(JobExecutors::getId, ids)
        ), () -> new SnailJobServerException("Failed to delete job executor"));
        return Boolean.TRUE;
    }
}
