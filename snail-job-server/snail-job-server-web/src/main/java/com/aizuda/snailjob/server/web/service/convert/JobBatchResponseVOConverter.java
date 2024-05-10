package com.aizuda.snailjob.server.web.service.convert;

import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.model.response.JobBatchResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.dataobject.JobBatchResponseDO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import com.aizuda.snailjob.template.datasource.persistence.po.JobTaskBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author: shuguang.zhang
 * @date : 2023-10-12 10:23
 * @since : 2.4.0
 */
@Mapper
public interface JobBatchResponseVOConverter {

    JobBatchResponseVOConverter INSTANCE = Mappers.getMapper(JobBatchResponseVOConverter.class);

    List<JobBatchResponseVO> convertList(List<JobBatchResponseDO> jobBatches);

    @Mappings({
        @Mapping(target = "executionAt", expression = "java(JobBatchResponseVOConverter.toLocalDateTime(jobBatchResponseDO.getExecutionAt()))")
    })
    JobBatchResponseVO convert(JobBatchResponseDO jobBatchResponseDO);

    List<JobBatchResponseVO> convertListToJobBatchList(List<JobTaskBatch> jobTaskBatchList);

    @Mappings({
            @Mapping(target = "executionAt", expression = "java(JobBatchResponseVOConverter.toLocalDateTime(jobTaskBatch.getExecutionAt()))")
    })
    JobBatchResponseVO convert(JobTaskBatch jobTaskBatch);

    @Mappings({
        @Mapping(source = "jobBatch.groupName", target = "groupName"),
        @Mapping(source = "jobBatch.id", target = "id"),
        @Mapping(source = "jobBatch.createDt", target = "createDt"),
        @Mapping(target = "executionAt", expression = "java(JobBatchResponseVOConverter.toLocalDateTime(jobBatch.getExecutionAt()))")
    })
    JobBatchResponseVO convert(JobTaskBatch jobBatch, Job job);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

}
