package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.web.model.request.JobRequestVO;
import com.aizuda.snail.job.template.datasource.persistence.po.Job;
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

    Job toJob(JobRequestVO jobRequestVO);

}
