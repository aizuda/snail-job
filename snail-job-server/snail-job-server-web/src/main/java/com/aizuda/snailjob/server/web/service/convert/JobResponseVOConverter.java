package com.aizuda.snailjob.server.web.service.convert;

import cn.hutool.core.util.StrUtil;
import com.aizuda.snailjob.common.core.util.JsonUtil;
import com.aizuda.snailjob.server.common.dto.PointInTimeDTO;
import com.aizuda.snailjob.server.common.strategy.WaitStrategies;
import com.aizuda.snailjob.server.common.util.DateUtils;
import com.aizuda.snailjob.server.web.model.request.JobRequestVO;
import com.aizuda.snailjob.server.web.model.response.JobResponseVO;
import com.aizuda.snailjob.template.datasource.persistence.po.Job;
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

    @Mappings({
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIds(job.getNotifyIds()))")
    })
    List<JobResponseVO> convertList(List<Job> jobs);

    @Mappings({
            @Mapping(target = "nextTriggerAt", expression = "java(JobResponseVOConverter.toLocalDateTime(job.getNextTriggerAt()))"),
            @Mapping(target = "notifyIds", expression = "java(JobConverter.toNotifyIds(job.getNotifyIds()))"),
            @Mapping(target = "triggerInterval", expression = "java(JobResponseVOConverter.toTriggerInterval(job))")
    })
    JobResponseVO convert(Job job);

    static LocalDateTime toLocalDateTime(Long nextTriggerAt) {
        if (Objects.isNull(nextTriggerAt) || nextTriggerAt == 0) {
            return null;
        }

        return DateUtils.toLocalDateTime(nextTriggerAt);
    }

    static String toTriggerInterval(Job job) {
        String triggerInterval = job.getTriggerInterval();
        Integer triggerType = job.getTriggerType();

        if (WaitStrategies.WaitStrategyEnum.POINT_IN_TIME.getType().equals(triggerType)) {
            List<PointInTimeDTO> pointInTimeDTOS = JsonUtil.parseList(triggerInterval, PointInTimeDTO.class);
            List<String> timeStrList = pointInTimeDTOS
                    .stream()
                    .map(time -> DateUtils.format(DateUtils.toLocalDateTime(time.getTime())))
                    .toList();
            return JsonUtil.toJsonString(timeStrList);
        }

        return triggerInterval;

    }
}
