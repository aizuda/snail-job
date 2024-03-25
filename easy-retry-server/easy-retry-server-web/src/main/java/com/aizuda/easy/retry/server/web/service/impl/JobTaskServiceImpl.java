package com.aizuda.easy.retry.server.web.service.impl;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobTaskQueryVO;
import com.aizuda.easy.retry.server.web.model.response.JobTaskResponseVO;
import com.aizuda.easy.retry.server.web.service.JobTaskService;
import com.aizuda.easy.retry.server.web.service.convert.JobTaskResponseVOConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTask;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
public class JobTaskServiceImpl implements JobTaskService {

    @Autowired
    private JobTaskMapper jobTaskMapper;

    @Override
    public PageResult<List<JobTaskResponseVO>> getJobTaskPage(final JobTaskQueryVO queryVO) {

        PageDTO<JobTask> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<JobTask> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(queryVO.getJobId())) {
            queryWrapper.eq(JobTask::getJobId, queryVO.getJobId());
        }

        if (Objects.nonNull(queryVO.getTaskBatchId())) {
            queryWrapper.eq(JobTask::getTaskBatchId, queryVO.getTaskBatchId());
        }

        queryWrapper.orderByAsc(JobTask::getJobId); // SQLServer 分页必须 ORDER BY
        PageDTO<JobTask> selectPage = jobTaskMapper.selectPage(pageDTO, queryWrapper);

        List<JobTaskResponseVO> jobTaskResponseVOs = JobTaskResponseVOConverter.INSTANCE.toJobTaskResponseVOs(
            selectPage.getRecords());
        for (JobTaskResponseVO jobTaskResponseVO : jobTaskResponseVOs) {
            jobTaskResponseVO.setKey(jobTaskResponseVO.getId());
        }

        return new PageResult<>(pageDTO, jobTaskResponseVOs);
    }
}
