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
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobExecutorMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobExecutor;
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
    private final JobExecutorMapper jobExecutorMapper;

    @Override
    public PageResult<List<JobExecutor>> getJobExecutorPage(JobExecutorQueryVO jobQueryVO) {
        PageDTO<JobExecutor> pageDTO = new PageDTO<>(jobQueryVO.getPage(), jobQueryVO.getSize());
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(jobQueryVO.getGroupName());

        PageDTO<JobExecutor> selectPage = jobExecutorMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<JobExecutor>()
                        .eq(JobExecutor::getNamespaceId, userSessionVO.getNamespaceId())
                        .eq(StrUtil.isNotBlank(jobQueryVO.getExecutorType()), JobExecutor::getExecutorType, jobQueryVO.getExecutorType())
                        .in(CollUtil.isNotEmpty(groupNames), JobExecutor::getGroupName, groupNames)
                        .like(StrUtil.isNotBlank(jobQueryVO.getExecutorInfo()), JobExecutor::getExecutorInfo, StrUtil.trim(jobQueryVO.getExecutorInfo()))
                        .orderByAsc(JobExecutor::getId));

        return new PageResult<>(pageDTO, selectPage.getRecords());
    }

    @Override
    public JobExecutor getJobExecutorDetail(Long id) {
        return jobExecutorMapper.selectById(id);
    }

    @Override
    public List<JobExecutor> getJobExecutorList(JobExecutorQueryVO jobQueryVO) {
        UserSessionVO userSessionVO = UserSessionUtils.currentUserSession();
        List<String> groupNames = UserSessionUtils.getGroupNames(jobQueryVO.getGroupName());

            return jobExecutorMapper.selectList(
                    new LambdaQueryWrapper<JobExecutor>()
                            .eq(JobExecutor::getNamespaceId, userSessionVO.getNamespaceId())
                            .eq(StrUtil.isNotBlank(jobQueryVO.getExecutorType()), JobExecutor::getExecutorType, jobQueryVO.getExecutorType())
                            .in(CollUtil.isNotEmpty(groupNames), JobExecutor::getGroupName, groupNames)
                            .like(StrUtil.isNotBlank(jobQueryVO.getExecutorInfo()), JobExecutor::getExecutorInfo, StrUtil.trim(jobQueryVO.getExecutorInfo()))
                            .orderByDesc(JobExecutor::getId));
    }

    @Override
    @Transactional
    public Boolean deleteJobExecutorByIds(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();
        Assert.isTrue(ids.size() == jobExecutorMapper.delete(
                new LambdaQueryWrapper<JobExecutor>()
                        .eq(JobExecutor::getNamespaceId, namespaceId)
                        .in(JobExecutor::getId, ids)
        ), () -> new SnailJobServerException("Failed to delete job executor"));
        return Boolean.TRUE;
    }
}
