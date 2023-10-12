package com.aizuda.easy.retry.server.web.service.impl;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobLogQueryVO;
import com.aizuda.easy.retry.server.web.model.response.JobLogResponseVO;
import com.aizuda.easy.retry.server.web.service.JobLogService;
import com.aizuda.easy.retry.server.web.service.convert.JobLogResponseVOConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobLogMessageMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 09:55
 * @since ：2.4.0
 */
@Service
public class JobLogServiceImpl implements JobLogService {

    @Autowired
    private JobLogMessageMapper jobLogMessageMapper;

    @Override
    public PageResult<List<JobLogResponseVO>> getJobLogPage(final JobLogQueryVO queryVO) {

        PageDTO<JobLogMessage> pageDTO = new PageDTO<>(queryVO.getPage(), queryVO.getSize());

        LambdaQueryWrapper<JobLogMessage> queryWrapper = new LambdaQueryWrapper<>();

        PageDTO<JobLogMessage> selectPage = jobLogMessageMapper.selectPage(pageDTO, queryWrapper);

        return new PageResult<>(pageDTO, JobLogResponseVOConverter.INSTANCE.toJobLogResponseVOs(selectPage.getRecords()));
    }
}
