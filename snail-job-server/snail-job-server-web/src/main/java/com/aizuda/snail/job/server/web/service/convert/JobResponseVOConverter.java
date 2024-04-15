package com.aizuda.snail.job.server.web.service.convert;

import com.aizuda.snail.job.server.common.util.DateUtils;
import com.aizuda.snail.job.server.web.model.response.JobResponseVO;
import com.aizuda.snail.job.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author opensnail
 * @date 2023-10-11 22:50:40
 * @since 2.4.0
 */
@Mapper
public interface JobResponseVOConverter {

    JobResponseVOConverter INSTANCE = Mappers.getMapper(JobResponseVOConverter.class);

//    @Mappings({
//        @Mapping(source = "nextTriggerAt", target = "nextTriggerAt", expression = "java(DateUtils.toLocalDateTime())")
//    })
    List<JobResponseVO> toJobResponseVOs(List<Job> jobs);

    @Mappings({
        @Mapping(target = "nextTriggerAt", expression = "java(JobResponseVOConverter.toLocalDateTime(job.getNextTriggerAt()))")
    })
    JobResponseVO toJobResponseVO(Job job);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }
}
