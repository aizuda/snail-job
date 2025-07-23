package com.aizuda.snailjob.server.service.convert;

import com.aizuda.snailjob.server.service.dto.JobBatchResponseBaseDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: shuguang.zhang
 * @date : 2023-10-12 10:23
 * @since : 2.4.0
 */
@Mapper
public interface JobBatchResponseConverter {

    JobBatchResponseConverter INSTANCE = Mappers.getMapper(JobBatchResponseConverter.class);

    @Mappings({
            @Mapping(source = "jobBatch.groupName", target = "groupName"),
            @Mapping(source = "jobBatch.id", target = "id"),
            @Mapping(source = "jobBatch.createDt", target = "createDt"),
            @Mapping(source = "jobBatch.updateDt", target = "updateDt"),
            @Mapping(target = "executionAt", expression = "java(JobBatchResponseConverter.toLocalDateTime(jobBatch.getExecutionAt()))")
    })
    JobBatchResponseBaseDTO convert(JobTaskBatch jobBatch, Job job);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        return JobConverter.toLocalDateTime(nextTriggerAt);
    }

    List<JobBatchResponseBaseDTO> convertListToJobBatchList(List<JobTaskBatch> jobTaskBatchList);

}
