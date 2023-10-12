package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.request.JobRequestVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author:  www.byteblogs.com
 * @date : 2023-10-12 09:40
 * @since : 2.4.0
 */
@Mapper
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    Job toJob(JobRequestVO jobRequestVO);

}
