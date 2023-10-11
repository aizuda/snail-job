package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.JobResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author www.byteblogs.com
 * @date 2023-10-11 22:50:40
 * @since 2.4.0
 */
@Mapper
public interface JobResponseVOConverter {

    JobResponseVOConverter INSTANCE = Mappers.getMapper(JobResponseVOConverter.class);

    List<JobResponseVO> toJobResponseVOs(List<Job> jobs);
}
