package com.aizuda.snailjob.server.common.convert;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.vo.JobResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author opensnail
 * @date 2023-10-11 22:50:40
 * @since 2.4.0
 */
@Mapper
@Deprecated
public interface JobResponseVOConverter {

    JobResponseVOConverter INSTANCE = Mappers.getMapper(JobResponseVOConverter.class);

    //    @Mappings({
//        @Mapping(source = "nextTriggerAt", target = "nextTriggerAt", expression = "java(DateUtils.toLocalDateTime())")
//    })
    List<JobResponseVO> convertList(List<Job> jobs);

    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(JobResponseVOConverter.toLocalDateTime(job.getNextTriggerAt()))"),
            @Mapping(target = "notifyIds", expression = "java(JobResponseVOConverter.toJobNotifyIds(job.getNotifyIds()))")
    })
    JobResponseVO convert(Job job);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

    static Set<Long> toJobNotifyIds(String notifyIds) {
        if (StrUtil.isBlank(notifyIds)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIds, Long.class));
    }
}
