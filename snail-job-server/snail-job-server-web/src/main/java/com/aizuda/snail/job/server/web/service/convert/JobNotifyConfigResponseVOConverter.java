package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.response.JobNotifyConfigResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.dataobject.JobNotifyConfigResponseDO;
import com.aizuda.snail.job.template.datasource.persistence.po.JobNotifyConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2022-03-03 11:20
 */
@Mapper
public interface JobNotifyConfigResponseVOConverter {

    JobNotifyConfigResponseVOConverter INSTANCE = Mappers.getMapper(JobNotifyConfigResponseVOConverter.class);

    JobNotifyConfigResponseVO convert(JobNotifyConfig jobNotifyConfig);

    List<JobNotifyConfigResponseVO> batchConvert(List<JobNotifyConfigResponseDO> jobNotifyConfigs);
}
