package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobNotifyConfigQueryVO;
import com.aizuda.easy.retry.server.web.model.request.JobNotifyConfigRequestVO;
import com.aizuda.easy.retry.server.web.model.response.JobNotifyConfigResponseVO;
import com.aizuda.easy.retry.server.web.service.JobNotifyConfigService;
import com.aizuda.easy.retry.server.web.service.convert.JobNotifyConfigConverter;
import com.aizuda.easy.retry.server.web.service.convert.JobNotifyConfigResponseVOConverter;
import com.aizuda.easy.retry.server.web.util.UserSessionUtils;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchQueryDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobNotifyConfigQueryDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobNotifyConfigResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.JobNotifyConfigMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobNotifyConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author: zuoJunLin
 * @date : 2023-12-02 12:54
 * @since ：2.5.0
 */
@Service
public class JobNotifyConfigServiceImpl implements JobNotifyConfigService {

    @Autowired
    private JobNotifyConfigMapper jobNotifyConfigMapper;

    @Override
    public PageResult<List<JobNotifyConfigResponseVO>> getJobNotifyConfigList(JobNotifyConfigQueryVO queryVO) {
        PageDTO<JobNotifyConfig> pageDTO = new PageDTO<>();
        JobNotifyConfigQueryDO jobNotifyConfigQueryDO = new JobNotifyConfigQueryDO();
        jobNotifyConfigQueryDO.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        if (StrUtil.isNotBlank(queryVO.getGroupName())) {
            jobNotifyConfigQueryDO.setGroupName(queryVO.getGroupName());
        }
        if (Objects.nonNull(queryVO.getJobId())) {
            jobNotifyConfigQueryDO.setJobId(queryVO.getJobId());
        }
        List<JobNotifyConfigResponseDO> batchResponseDOList = jobNotifyConfigMapper.selectJobNotifyConfigList(pageDTO, jobNotifyConfigQueryDO);
        return new PageResult<>(pageDTO, JobNotifyConfigResponseVOConverter.INSTANCE.batchConvert(batchResponseDOList));
    }

    @Override
    public Boolean saveJobNotify(JobNotifyConfigRequestVO requestVO) {
        JobNotifyConfig jobNotifyConfig = JobNotifyConfigConverter.INSTANCE.toJobNotifyConfig(requestVO);
        jobNotifyConfig.setCreateDt(LocalDateTime.now());
        jobNotifyConfig.setNamespaceId(UserSessionUtils.currentUserSession().getNamespaceId());
        Assert.isTrue(1 == jobNotifyConfigMapper.insert(jobNotifyConfig),
                () -> new EasyRetryServerException("failed to insert jobNotifyConfig. sceneConfig:[{}]", JsonUtil.toJsonString(jobNotifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateJobNotify(JobNotifyConfigRequestVO requestVO) {
        Assert.notNull(requestVO.getId(), () -> new EasyRetryServerException("参数异常"));
        JobNotifyConfig jobNotifyConfig = JobNotifyConfigConverter.INSTANCE.toJobNotifyConfig(requestVO);
        // 防止被覆盖
        jobNotifyConfig.setNamespaceId(null);
        Assert.isTrue(1 == jobNotifyConfigMapper.updateById(jobNotifyConfig),
                () -> new EasyRetryServerException("failed to update jobNotifyConfig. sceneConfig:[{}]", JsonUtil.toJsonString(jobNotifyConfig)));
        return Boolean.TRUE;
    }

    @Override
    public JobNotifyConfigResponseVO getJobNotifyConfigDetail(Long id) {
        JobNotifyConfig jobNotifyConfig = jobNotifyConfigMapper.selectOne(new LambdaQueryWrapper<JobNotifyConfig>()
                .eq(JobNotifyConfig::getId, id));
        return JobNotifyConfigResponseVOConverter.INSTANCE.convert(jobNotifyConfig);
    }
}
