package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.request.JobNotifyConfigRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.po.JobNotifyConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: zuoJunLin
 * @date : 2023-12-02 13:43
 */
@Mapper
public interface JobNotifyConfigConverter {

    JobNotifyConfigConverter INSTANCE = Mappers.getMapper(JobNotifyConfigConverter.class);

    JobNotifyConfig toJobNotifyConfig(JobNotifyConfigRequestVO jobNotifyConfigVO);
}
