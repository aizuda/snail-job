package com.aizuda.easy.retry.server.web.service.impl;

import com.aizuda.easy.retry.common.core.enums.StatusEnum;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobBatchQueryVO;
import com.aizuda.easy.retry.server.web.model.response.JobBatchResponseVO;
import com.aizuda.easy.retry.server.web.service.JobBatchService;
import com.aizuda.easy.retry.server.web.service.convert.JobBatchResponseVOConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobTaskBatchMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
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
public class JobBatchServiceImpl implements JobBatchService {

    @Autowired
    private JobTaskBatchMapper jobTaskBatchMapper;
    @Autowired
    private JobMapper jobMapper;

    @Override
    public PageResult<List<JobBatchResponseVO>> getJobBatchPage(final JobBatchQueryVO queryVO) {

        PageDTO<JobTaskBatch> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<JobTaskBatch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobTaskBatch::getDeleted, StatusEnum.NO.getStatus());

        if (Objects.nonNull(queryVO.getJobId())) {
            queryWrapper.eq(JobTaskBatch::getJobId, queryVO.getJobId());
        }

        PageDTO<JobTaskBatch> selectPage = jobTaskBatchMapper.selectPage(pageDTO, queryWrapper);

        List<JobBatchResponseVO> batchResponseVOList = JobBatchResponseVOConverter.INSTANCE.toJobBatchResponseVOs(
            selectPage.getRecords());

        return new PageResult<>(pageDTO, batchResponseVOList);
    }

    @Override
    public JobBatchResponseVO getJobBatchDetail(final Long id) {
        JobTaskBatch jobTaskBatch = jobTaskBatchMapper.selectById(id);
        if (Objects.isNull(jobTaskBatch)) {
            return null;
        }

        Job job = jobMapper.selectById(jobTaskBatch.getJobId());
        return JobBatchResponseVOConverter.INSTANCE.toJobBatchResponseVO(jobTaskBatch, job);
    }
}
