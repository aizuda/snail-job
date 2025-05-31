package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.PointInTimeDTO;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:40
 * @since : 2.4.0
 */
@Mapper
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    List<JobRequestVO> convertList(List<Job> jobs);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIds(job.getNotifyIds()))")
    })
    JobRequestVO convert(Job job);

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIdsStr(jobRequestVO.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(JobConverter.toTriggerInterval(jobRequestVO))")
    })
    Job convert(JobRequestVO jobRequestVO);

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

    static String toTriggerInterval(JobRequestVO jobRequestVO) {
        String triggerInterval = jobRequestVO.getTriggerInterval();
        if (StrUtil.isBlank(triggerInterval) || Objects.isNull(jobRequestVO.getTriggerType())) {
            return StrUtil.EMPTY;
        }

        if (jobRequestVO.getTriggerType() == WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType()) {
            List<String> pointInTimeDTOS = JsonUtil.parseList(triggerInterval, String.class);
            List<PointInTimeDTO> localDateTimes = pointInTimeDTOS
                    .stream()
                    .map(DateUtils::toLocalDateTime)
                    .map(DateUtils::toEpochMilli)
                    .map(time -> {
                        PointInTimeDTO pointInTimeDTO = new PointInTimeDTO();
                        pointInTimeDTO.setTime(time);
                        return pointInTimeDTO;
                    }).toList();
            return JsonUtil.toJsonString(localDateTimes);
        }

        return triggerInterval;
    }
}
