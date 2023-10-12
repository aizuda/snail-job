package com.aizuda.easy.retry.server.web.service.convert;

import com.aizuda.easy.retry.server.web.model.response.JobBatchResponseVO;
import com.aizuda.easy.retry.template.datasource.persistence.po.JobTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author: shuguang.zhang
 * @date : 2023-10-12 10:23
 * @since : 2.4.0
 */
@Mapper
public interface JobBatchResponseVOConverter {

    JobBatchResponseVOConverter INSTANCE = Mappers.getMapper(JobBatchResponseVOConverter.class);

    List<JobBatchResponseVO> toJobBatchResponseVOs(List<JobTaskBatch> jobBatches);

    JobBatchResponseVO toJobBatchResponseVO(JobTaskBatch jobBatch);
}
