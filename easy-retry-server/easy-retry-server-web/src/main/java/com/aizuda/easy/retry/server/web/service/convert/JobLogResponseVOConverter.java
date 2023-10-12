package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.JobLogResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobLogMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: www.byteblogs.com
 * @date : 2023-10-12 11:27
 * @since : 2.4.0
 */
@Mapper
public interface JobLogResponseVOConverter {
    JobLogResponseVOConverter INSTANCE = Mappers.getMapper(JobLogResponseVOConverter.class);

    List<JobLogResponseVO> toJobLogResponseVOs(List<JobLogMessage> jobLogMessages);

}
