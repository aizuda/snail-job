package com.aizuda.snail.job.template.datasource.persistence.mapper;

import com.aizuda.snail.job.template.datasource.persistence.dataobject.JobNotifyConfigResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.po.JobNotifyConfig;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zuoJunLin
 */
public interface JobNotifyConfigMapper extends BaseMapper<JobNotifyConfig> {
    List<JobNotifyConfigResponseDO> selectJobNotifyConfigList(IPage<JobNotifyConfig> iPage, @Param("ew") Wrapper<JobNotifyConfig> wrapper);
}
