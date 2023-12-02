package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.JobNotifyConfigResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.dataobject.JobNotifyConfigResponseDO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobNotifyConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2022-03-03 11:20
 */
@Mapper
public interface JobNotifyConfigResponseVOConverter {

    JobNotifyConfigResponseVOConverter INSTANCE = Mappers.getMapper(JobNotifyConfigResponseVOConverter.class);

    JobNotifyConfigResponseVO convert(JobNotifyConfig jobNotifyConfig);

    List<JobNotifyConfigResponseVO> batchConvert(List<JobNotifyConfigResponseDO> jobNotifyConfigs);
}
