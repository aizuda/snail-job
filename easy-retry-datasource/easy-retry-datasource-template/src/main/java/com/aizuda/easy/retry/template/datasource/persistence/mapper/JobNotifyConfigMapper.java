package com.aizuda.easy.retry.template.datasource.persistence.mapper;

import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobNotifyConfigQueryDO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobNotifyConfigResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobNotifyConfig;
import com.aizuda.easy.retry.template.datasource.persistence.po.NotifyConfig;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zuoJunLin
 */
public interface JobNotifyConfigMapper extends BaseMapper<JobNotifyConfig> {
    List<JobNotifyConfigResponseDO> selectJobNotifyConfigList(IPage<JobNotifyConfig> iPage, @Param("ew") Wrapper<JobNotifyConfig> wrapper);
}
