package com.aizuda.snailjob.server.service.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.common.util.TriggerIntervalUtils;
import com.aizuda.snailjob.server.service.dto.JobRequestDTO;
import com.aizuda.snailjob.server.service.dto.JobResponseDTO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 *
 * </p>
 *
 * @author opensnail
 * @date 2025-07-05
 */
@Mapper
public interface JobConverter {
    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIdsStr(jobRequestDTO.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(JobConverter.toTriggerInterval(jobRequestDTO))")
    })
    Job convert(JobRequestDTO jobRequestDTO);


    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(com.aizuda.snailjob.server.common.util.DateUtils.toLocalDateTime(job.getNextTriggerAt()))"),
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIds(job.getNotifyIds()))")
    })
    void fillCommonFields(Job job, @MappingTarget JobResponseDTO target);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

    static Set<Long> toNotifyIds(String notifyIds) {
        if (StrUtil.isBlank(notifyIds)) {
            return new HashSet<>();
        }

        return new HashSet<>(JsonUtil.parseList(notifyIds, Long.class));
    }

    static String toNotifyIdsStr(Set<Long> notifyIds) {
        if (CollUtil.isEmpty(notifyIds)) {
            return StrUtil.EMPTY;
        }

        return JsonUtil.toJsonString(notifyIds);
    }

    static String toTriggerInterval(JobRequestDTO jobRequestVO) {
        String triggerInterval = jobRequestVO.getTriggerInterval();
        if (StrUtil.isBlank(triggerInterval) || Objects.isNull(jobRequestVO.getTriggerType())) {
            return StrUtil.EMPTY;
        }

        return TriggerIntervalUtils.getPointInTimeStr(triggerInterval, jobRequestVO.getTriggerType());
    }
}

