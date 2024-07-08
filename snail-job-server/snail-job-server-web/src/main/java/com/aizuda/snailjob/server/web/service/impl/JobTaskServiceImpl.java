package com.aizuda.snailjob.server.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.aizuda.snailjob.common.core.util.StreamUtils;
import com.aizuda.snailjob.server.common.exception.SnailJobServerException;
import com.aizuda.snailjob.server.web.model.base.PageResult;
import com.aizuda.snailjob.server.web.model.request.JobTaskQueryVO;
import com.aizuda.snailjob.server.web.model.response.JobTaskResponseVO;
import com.aizuda.snailjob.server.web.service.JobTaskService;
import com.aizuda.snailjob.server.web.service.convert.JobTaskResponseVOConverter;
import com.aizuda.snailjob.server.web.util.UserSessionUtils;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.snailjob.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.snailjob.template.datasource.persistence.po.JobLogMessage;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
@RequiredArgsConstructor
public class JobTaskServiceImpl implements JobTaskService {
    private final JobTaskMapper jobTaskMapper;
    private final JobLogMessageMapper jobLogMessageMapper;

    @Override
    public PageResult<List<JobTaskResponseVO>> getJobTaskPage(final JobTaskQueryVO queryVO) {

        PageDTO<JobTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());
        PageDTO<JobTask> selectPage = jobTaskMapper.selectPage(pageDTO,
                new LambdaQueryWrapper<JobTask>()
                        .eq(Objects.nonNull(queryVO.getJobId()), JobTask::getJobId, queryVO.getJobId())
                        .eq(Objects.nonNull(queryVO.getTaskBatchId()), JobTask::getTaskBatchId, queryVO.getTaskBatchId())
                        .eq(Objects.nonNull(queryVO.getTaskStatus()), JobTask::getTaskStatus, queryVO.getTaskStatus())
                        .eq(JobTask::getParentId, 0)
                        // SQLServer 分页必须 ORDER BY
                        .orderByAsc(JobTask::getId));

        List<JobTask> records = selectPage.getRecords();

        return new PageResult<>(pageDTO, convertJobTaskList(records));
    }

    @Override
    public List<JobTaskResponseVO> getTreeJobTask(final JobTaskQueryVO queryVO) {
        List<JobTask> taskList = jobTaskMapper.selectList(
                new LambdaQueryWrapper<JobTask>()
                        .eq(Objects.nonNull(queryVO.getParentId()), JobTask::getParentId, queryVO.getParentId())
                        .eq(Objects.nonNull(queryVO.getJobId()), JobTask::getJobId, queryVO.getJobId())
                        .eq(Objects.nonNull(queryVO.getTaskBatchId()), JobTask::getTaskBatchId, queryVO.getTaskBatchId())
                        // SQLServer 分页必须 ORDER BY
                        .orderByAsc(JobTask::getJobId));

        return convertJobTaskList(taskList);
    }

    @Override
    public Boolean deleteJobTaskById(Set<Long> ids) {
        String namespaceId = UserSessionUtils.currentUserSession().getNamespaceId();

        Assert.isTrue(ids.size() == jobTaskMapper.delete(
                new LambdaQueryWrapper<JobTask>()
                        .eq(JobTask::getNamespaceId, namespaceId)
                        .in(JobTask::getId, ids)
        ), () -> new SnailJobServerException("删除任务批次失败"));

        // 删除日志信息
        jobLogMessageMapper.delete(new LambdaQueryWrapper<JobLogMessage>()
                .eq(JobLogMessage::getNamespaceId, namespaceId)
                .eq(JobLogMessage::getTaskId, ids)
        );

        return Boolean.TRUE;
    }

    private List<JobTaskResponseVO> convertJobTaskList(List<JobTask> taskList) {
        if (CollUtil.isEmpty(taskList)) {
            return new ArrayList<>();
        }

        List<JobTaskResponseVO> jobTaskResponseVOs = JobTaskResponseVOConverter.INSTANCE.convertList(
                taskList);

        Set<Long> parentIds = StreamUtils.toSet(jobTaskResponseVOs, JobTaskResponseVO::getId);
        List<JobTask> jobTasks = jobTaskMapper.selectList(new LambdaQueryWrapper<JobTask>()
                .select(JobTask::getParentId).in(JobTask::getParentId, parentIds));
        Set<Long> jobTaskParentIds = StreamUtils.toSet(jobTasks, JobTask::getParentId);
        jobTaskResponseVOs.forEach(jobTask -> jobTask.setChildNode(!jobTaskParentIds.contains(jobTask.getId())));

        return jobTaskResponseVOs;
    }

}
