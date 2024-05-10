package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author:  opensnail
 * @date : 2023-10-12 09:40
 * @since : 2.4.0
 */
@Mapper
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    Job convert(JobRequestVO jobRequestVO);

}
